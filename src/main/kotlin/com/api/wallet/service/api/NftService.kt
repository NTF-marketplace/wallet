package com.api.wallet.service.api

import com.api.wallet.domain.nft.Nft
import com.api.wallet.domain.nft.repository.NftRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.domain.walletNft.WalletNft
import com.api.wallet.domain.walletNft.repository.WalletNftDto
import com.api.wallet.domain.walletNft.repository.WalletNftRepository
import com.api.wallet.enums.ChainType
import com.api.wallet.service.external.moralis.MoralisApiService
import com.api.wallet.service.external.moralis.dto.response.NFTResult
import com.api.wallet.service.external.nft.NftApiService
import com.api.wallet.service.external.nft.dto.NftRequest
import com.api.wallet.service.external.nft.dto.NftResponse
import com.api.wallet.service.external.nft.dto.NftResponse.Companion.toEntity
import com.api.wallet.util.Util.convertNetworkTypeToChainType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class NftService(
    private val nftRepository: NftRepository,
    private val walletRepository: WalletRepository,
    private val walletNftRepository: WalletNftRepository,
    private val nftApiService: NftApiService,
) {

    fun save(response: NftResponse): Mono<Void> {
        return nftRepository.findById(response.id)
            .switchIfEmpty(
            nftRepository.insert(response.toEntity())
        ).then()
    }

    fun findOrCreateNft(nftId: Long,tokenAddress: String, tokenId: String, chainType: ChainType): Mono<Nft> {
        return nftRepository.findById(nftId)
            .switchIfEmpty(
                nftApiService.getNftSave(
                    NftRequest(
                        nftId,
                        tokenAddress,
                        tokenId,
                        chainType
                    )
                ).flatMap { nftRepository.insert(it.toEntity()) }
            )
    }

    @Transactional
    fun readAllNftByWallet(address: String, chainType:ChainType?): Flux<NftResponse> {
        val wallets = if (chainType != null) {
            walletRepository.findByAddressAndChainType(address, chainType).flux()
        } else {
            walletRepository.findAllByAddress(address)
        }
        return wallets
            .flatMap { wallet ->
                getNftByWallet(wallet)
            }
    }


    private fun getNftByWallet(wallet: Wallet): Flux<NftResponse> {
        val response = nftApiService.getByWalletNft(wallet.address, wallet.chainType)
        val walletNfts = walletNftRepository.findByWalletIdJoinNft(wallet.address,wallet.chainType)

        return response.collectList().flatMapMany { responseList ->
            val responseIds = responseList.map { it.id }
            walletNfts.collectList().flatMapMany { walletNftList ->
                val currentNftIds = walletNftList.map { it.nftId }

                deleteToWalletNft(responseIds, currentNftIds, wallet)
                    .thenMany(addToWalletNft(responseList, currentNftIds, wallet))
                    .thenMany(Flux.fromIterable(responseList))
            }
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