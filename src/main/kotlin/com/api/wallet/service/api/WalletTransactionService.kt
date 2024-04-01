package com.api.wallet.service.api

import com.api.wallet.domain.transaction.Transaction
import com.api.wallet.domain.transaction.repository.TransactionRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.NetworkType
import com.api.wallet.service.external.moralis.MoralisService
import com.api.wallet.service.external.moralis.dto.response.TransferResult
import com.api.wallet.util.Util.convertNetworkTypeToChainType
import com.api.wallet.util.Util.toIsoString
import com.api.wallet.util.Util.toTimestamp
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class WalletTransactionService(
    private val moralisService: MoralisService,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val nftService: NftService,
) {
    //TODO("반환값 재정의 : nft 메타데이터 필요")
    @Transactional
    fun readAllTransactions(address: String, networkType: NetworkType?, pageable: Pageable): Mono<Page<Transaction>> {
        val wallets = if (networkType != null) {
            walletRepository.findByAddressAndNetworkType(address, networkType.toString()).flux()
        } else {
            walletRepository.findAllByAddress(address)
        }

        return wallets
            .flatMap { wallet ->
                getTransactions(wallet, pageable)
            }
            .collectList()
            .zipWith(transactionRepository.count())
            .map { PageImpl(it.t1, pageable, it.t2) }
    }

    private fun getTransactions(wallet: Wallet, pageable: Pageable): Flux<Transaction> {
        val updateFlux = transactionRepository.findAllByWalletIdOrderByBlockTimestampDesc(wallet.id!!,pageable)
            .collectList()
            .flatMapMany {
                val lastBlockTimestamp = it.firstOrNull()?.blockTimestamp?.plus(10000)
                moralisService.getWalletNFTTransfers(
                    wallet.address,
                    wallet.networkType.convertNetworkTypeToChainType(),
                    lastBlockTimestamp?.toIsoString() ?: null,
                    System.currentTimeMillis().toIsoString()
                )
                    .flatMapMany { Flux.fromIterable(it.result) }
                    .filter { !it.possibleSpam }
                    .flatMap { result ->
                        saveOrUpdate(Flux.just(result), wallet)
                    }
            }

        return updateFlux.thenMany(
            transactionRepository.findAllByWalletIdOrderByBlockTimestampDesc(wallet.id!!, pageable)
        )
    }



    private fun saveOrUpdate(results: Flux<TransferResult>, wallet: Wallet): Flux<Transaction> {
       return results.flatMap {result ->
           nftService.findOrCreateNft(result.tokenAddress,wallet.networkType,result.tokenId).flatMap { nft->
               transactionRepository.save(
                   Transaction(
                       nftId = nft.id!!,
                       toAddress = result.toAddress,
                       fromAddress = result.fromAddress,
                       amount = result.amount.toInt(),
                       value = result.value.toBigDecimal(),
                       hash =  result.blockHash,
                       blockTimestamp = result.blockTimestamp.toTimestamp(),
                       walletId = wallet.id!!
                   )
               )
           }
        }.collectList()
           .flatMapMany { Flux.fromIterable(it) }
    }

}