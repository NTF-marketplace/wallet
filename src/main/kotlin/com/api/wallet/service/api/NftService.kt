package com.api.wallet.service.api

import com.api.wallet.RedisService
import com.api.wallet.controller.dto.response.NftMetadataResponse
import com.api.wallet.domain.nft.Nft
import com.api.wallet.domain.nft.repository.NftRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.walletNft.WalletNft
import com.api.wallet.domain.walletNft.repository.WalletNftRepository
import com.api.wallet.enums.ChainType
import com.api.wallet.service.ErrorHandlerService
import com.api.wallet.service.external.nft.NftApiService
import com.api.wallet.service.external.nft.dto.NftRequest
import com.api.wallet.service.external.nft.dto.NftResponse
import com.api.wallet.service.external.nft.dto.NftResponse.Companion.toEntity
import com.api.wallet.util.Util.toPagedMono
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors

@Service
class NftService(
    private val nftRepository: NftRepository,
    private val walletNftRepository: WalletNftRepository,
    private val nftApiService: NftApiService,
    private val walletService: WalletService,
    private val redisService: RedisService,
    // private val errorHandlerService: ErrorHandlerService,
) {

    private val virtualThreadScheduler = Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor())

    fun save(response: NftResponse): Mono<Void> {
        return nftRepository.findById(response.id)
            .switchIfEmpty(
                nftRepository.insert(response.toEntity())
            ).then()
    }

    fun findOrCreateNft(nftId: Long, tokenAddress: String, tokenId: String, chainType: ChainType): Mono<Nft> {
        return nftRepository.findById(nftId)
            .switchIfEmpty(
                nftApiService.getNftSave(
                    NftRequest(
                        tokenAddress,
                        tokenId,
                        chainType
                    )
                ).flatMap {
                    nftRepository.insert(it.toEntity()) }
            )
    }

    fun readAllNftByWallet(address: String, chainType: ChainType?, pageable: Pageable): Mono<Page<NftMetadataResponse>> {
        return walletService.findWallet(address, chainType)
            .flatMap { wallet ->
                getNftByWallet(wallet)
                    .map { it.id }
                    .collectList()
                    .flatMapMany { nftIds ->
                        redisService.getNfts(nftIds)
                    }
            }.let{
                toPagedMono( it, pageable)
            }
    }


    private fun getNftByWallet(wallet: Wallet): Flux<NftResponse> {
        val responseFlux = nftApiService.getByWalletNft(wallet.address, wallet.chainType)
            .subscribeOn(virtualThreadScheduler)

        val walletNftsFlux = walletNftRepository.findByWalletIdJoinNft(wallet.address, wallet.chainType)
            .subscribeOn(virtualThreadScheduler)

        return Flux.zip(responseFlux.collectList(), walletNftsFlux.collectList()).flatMap { tuple ->
            val responseList = tuple.t1
            val walletNftList = tuple.t2

            val responseIds = responseList.map { it.id }
            val currentNftIds = walletNftList.map { it.nftId }

            deleteToWalletNft(responseIds, currentNftIds, wallet)
                .thenMany(addToWalletNft(responseList, currentNftIds, wallet))
                .thenMany(Flux.fromIterable(responseList))
        }
    }

    private fun addToWalletNft(
        newNfts: List<NftResponse>,
        oldIds: List<Long>,
        wallet: Wallet
    ): Flux<WalletNft> {
        val nftsToAdd = newNfts.filter { it.id !in oldIds }

        return Flux.fromIterable(nftsToAdd)
            .flatMap { nftResponse ->
                findOrCreateNft(nftResponse.id, nftResponse.tokenAddress, nftResponse.tokenId, nftResponse.chainType)
                    .flatMap {
                        walletNftRepository.save(
                            WalletNft(
                                walletId = wallet.id!!,
                                nftId = it.id,
                                amount = 0
                            )
                        )
                    }
            }
    }
    private fun deleteToWalletNft(
        newIds: List<Long>,
        oldIds: List<Long>,
        wallet: Wallet
    ): Flux<Void> {

        val idsToDelete = oldIds.filter { it !in newIds }

        return Flux.fromIterable(idsToDelete)
            .flatMap { nftId ->
                walletNftRepository.deleteByNftIdAndWalletId(nftId, wallet.id!!)
            }
    }

}