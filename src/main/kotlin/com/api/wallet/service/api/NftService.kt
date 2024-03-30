package com.api.wallet.service.api

import com.api.wallet.domain.nft.Nft
import com.api.wallet.domain.nft.repository.NftRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.domain.walletNft.WalletNft
import com.api.wallet.domain.walletNft.repository.WalletNftRepository
import com.api.wallet.enums.NetworkType
import com.api.wallet.service.moralis.MoralisService
import com.api.wallet.service.moralis.dto.response.NFTResult
import com.api.wallet.util.Util.convertNetworkTypeToChainType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class NftService(
    private val nftRepository: NftRepository,
    private val walletRepository: WalletRepository,
    private val moralisService: MoralisService,
    private val walletNftRepository: WalletNftRepository,
) {

    fun findByTokenAddress(tokenAddress: String,networkType: String): Mono<Nft> {
        return nftRepository.findByTokenAddressAndNetworkType(tokenAddress,networkType)
            .switchIfEmpty(
                nftRepository.insert(Nft(tokenAddress,networkType)
            ))
    }


    //TODO("반환값 재정의 : nft: 메타데이터 필요")
    @Transactional
    fun readAllNftByWallet(address: String, networkType:NetworkType?, pageable: Pageable): Mono<Page<WalletNft>> {
        val wallets = if (networkType != null) {
            walletRepository.findByAddressAndNetworkType(address, networkType.toString()).flux()
        } else {
            walletRepository.findAllByAddress(address)
        }

        return wallets
            .flatMap { wallet ->
                getNftByWallet(wallet)
            }
            .collectList()
            .map {
                val start = pageable.offset.toInt()
                val end = (start + pageable.pageSize).coerceAtMost(it.size)
                val page = if (start <= end) it.subList(start, end) else listOf()
                PageImpl(page, pageable, it.size.toLong())
            }
    }

    private fun getNftByWallet(wallet: Wallet): Flux<WalletNft> {
        val response = moralisService.getNFTsByAddress(wallet.address, wallet.networkType.convertNetworkTypeToChainType()) //11
        val getNftsByWallet = walletNftRepository.findByWalletId(wallet.address)

        return Mono.zip(response, getNftsByWallet.collectList())
            .flatMapMany { tuple ->
                val responseNfts = tuple.t1.result.associateBy { it.tokenAddress }
                val getNfts = tuple.t2.associateBy { it.nftId } // 0

                deleteToWalletNft(responseNfts, getNfts, wallet)
                    .thenMany(addToWalletNft(responseNfts, getNfts, wallet))
                    .thenMany(walletNftRepository.findByWalletId(wallet.address))
            }
    }


    private fun addToWalletNft(
        responseNftsMap: Map<String,NFTResult>,
        getNftsMap: Map<String,WalletNft>,
        wallet: Wallet,
        ): Flux<WalletNft> {
          return Flux.fromIterable(responseNftsMap.keys).filter{ !getNftsMap.containsKey(it) }
                .flatMap { tokenAddress ->
                     findByTokenAddress(tokenAddress,wallet.networkType).flatMap { nft->
                         walletNftRepository.save(
                             WalletNft(
                                 walletId = wallet.address,
                                 nftId = nft.tokenAddress
                             )
                         )
                     }
                }
        }

    private fun deleteToWalletNft(
        responseNftsMap: Map<String,NFTResult>,
        getNftsMap: Map<String,WalletNft>,
        wallet: Wallet,
    ): Flux<Void>
    {
       return Flux.fromIterable(getNftsMap.keys)
                .filter { !responseNftsMap.containsKey(it) }
                .flatMap { tokenAddress ->
                    walletNftRepository.deleteByNftIdAndWalletId(tokenAddress,wallet.address)
                }
    }

}