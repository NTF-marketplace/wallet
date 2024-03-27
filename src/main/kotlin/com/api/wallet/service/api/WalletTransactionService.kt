package com.api.wallet.service.api

import com.api.wallet.domain.transaction.Transaction
import com.api.wallet.domain.transaction.repository.TransactionRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.service.moralis.MoralisService
import com.api.wallet.service.moralis.dto.response.TransferResult
import com.api.wallet.util.Util.convertNetworkTypeToChainType
import com.api.wallet.util.Util.toIsoString
import com.api.wallet.util.Util.toTimestamp
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux

@Service
class WalletTransactionService(
    private val moralisService: MoralisService,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val nftService: NftService,
) {

    @Transactional
    fun readAllTransactions(address: String) : Flux<Transaction> {
        return walletRepository.findAllByAddress(address).flatMap { wallet->
            getTransactions(wallet)
        }
    }

    private fun getTransactions(wallet: Wallet) : Flux<Transaction> {
        val transactions = transactionRepository.findAllByWalletIdOrderByBlockTimestampDesc(wallet.address)
            .collectList()

       return transactions.flatMapMany { transaction ->
            val lastBlockTimestamp = transaction.firstOrNull()?.blockTimestamp?.plus(10000)

            val response = moralisService.getWalletNFTTransfers(
                wallet.address,
                wallet.networkType.convertNetworkTypeToChainType(),
                lastBlockTimestamp?.toIsoString() ?: null,
                System.currentTimeMillis().toIsoString()
            ).flatMapMany { Flux.fromIterable(it.result) }
                .filter { !it.possibleSpam }

          saveOrUpdate(response,wallet)
        }
    }


    private fun saveOrUpdate(results: Flux<TransferResult>,wallet: Wallet): Flux<Transaction> {
       return results.flatMap {result ->
           nftService.findByTokenAddress(result.tokenAddress,wallet.networkType).flatMap { nft->
               transactionRepository.save(
                   Transaction(
                       nftId = nft.tokenAddress,
                       toAddress = result.toAddress,
                       fromAddress = result.fromAddress,
                       amount = result.amount.toInt(),
                       value = result.value.toBigDecimal(),
                       hash =  result.blockHash,
                       blockTimestamp = result.blockTimestamp.toTimestamp(),
                       walletId = wallet.address
                   )
               )
           }

        }
    }

}