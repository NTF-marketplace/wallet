package com.api.wallet.service.api

import com.api.wallet.domain.nft.Nft
import com.api.wallet.domain.nft.repository.NftRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.domain.walletNft.WalletNft
import com.api.wallet.domain.walletNft.repository.WalletNftDto
import com.api.wallet.domain.walletNft.repository.WalletNftRepository
import com.api.wallet.enums.NetworkType
import com.api.wallet.service.external.moralis.MoralisApiService
import com.api.wallet.service.external.moralis.dto.response.NFTResult
import com.api.wallet.service.external.nft.NftApiService
import com.api.wallet.service.external.nft.dto.NftResponse
import com.api.wallet.util.Util.convertNetworkTypeToChainType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class NftService(
    private val nftRepository: NftRepository,
    private val walletRepository: WalletRepository,
    private val moralisApiService: MoralisApiService,
    private val walletNftRepository: WalletNftRepository,
    private val nftApiService: NftApiService,
) {

    fun findOrCreateNft(tokenAddress: String,
                        networkType: String,
                        tokenId:String,
                        originId: Long,
                        contractType: String
    ): Mono<Nft> {
        return nftRepository.findByTokenAddressAndNetworkTypeAndTokenId(tokenAddress,networkType,tokenId)
            .switchIfEmpty(
                nftRepository.insert(
                    Nft(
                        id = originId,
                        tokenId = tokenId,
                        tokenAddress = tokenAddress,
                        networkType = networkType,
                        contractType = contractType
                    )
                )
            )
    }

    @Transactional
    fun readAllNftByWallet(address: String, networkType:NetworkType?): Flux<NftResponse> {
        val wallets = if (networkType != null) {
            walletRepository.findByAddressAndNetworkType(address, networkType.toString()).flux()
        } else {
            walletRepository.findAllByAddress(address)
        }
        return wallets
            .flatMap { wallet ->
                getNftByWallet(wallet)
            }
    }

    private fun getNftByWallet(wallet: Wallet): Flux<NftResponse> {
        val response = moralisApiService.getNFTsByAddress(wallet.address, wallet.networkType.convertNetworkTypeToChainType())
        val getNftsByWallet = walletNftRepository.findByWalletIdJoinNft(wallet.address,wallet.networkType)

        return Mono.zip(response, getNftsByWallet.collectList())
            .flatMapMany { tuple ->
                val responseNfts = tuple.t1.result
                    .filter { it.contractType == "ERC721" }
                    .associateBy { Pair(it.tokenAddress, it.tokenId) }

                val getNfts = tuple.t2.associateBy { Pair(it.nftTokenAddress,it.nftTokenId) }

                deleteToWalletNft(responseNfts, getNfts, wallet)
                    .thenMany(addToWalletNft(responseNfts, getNfts, wallet))
                    .thenMany(walletNftRepository.findByWalletId(wallet.id!!)
                        .map{ it.nftId }
                        .collectList()
                        .filterWhen { ids -> Mono.just(ids.isNotEmpty()) }
                        .flatMapMany { ids ->
                            nftApiService.getNfts(ids)
                        }
                        .switchIfEmpty(Flux.empty())
                    )

            }
    }

    private fun addToWalletNft(
        responseNftsMap: Map<Pair<String, String>, NFTResult>,
        getNftsMap: Map<Pair<String, String>, WalletNftDto>,
        wallet: Wallet
    ): Flux<WalletNft> {
        val nftDataToSave = responseNftsMap.filterKeys { !getNftsMap.containsKey(it) }
            .values.toList()

        return nftApiService.saveNfts(nftDataToSave,wallet.networkType.convertNetworkTypeToChainType())
            .collectList()
            .flatMapMany {
                Flux.fromIterable(it).flatMap { savedNft ->
                    val originalNftData = responseNftsMap[Pair(savedNft.tokenAddress, savedNft.tokenId)]
                    findOrCreateNft(savedNft.tokenAddress, wallet.networkType, savedNft.tokenId, savedNft.id, savedNft.contractType)
                        .flatMap { nft ->
                            walletNftRepository.save(
                                WalletNft(
                                    walletId = wallet.id!!,
                                    nftId = nft.id,
                                    amount = originalNftData?.amount?.toInt() ?: 0
                                )
                            )
                        }
                }
            }
    }



    private fun deleteToWalletNft(
        responseNftsMap: Map<Pair<String,String>, NFTResult>,
        getNftsMap: Map<Pair<String,String>, WalletNftDto>,
        wallet: Wallet,
    ): Flux<Void>
    {
       return Flux.fromIterable(getNftsMap.keys)
                .filter { !responseNftsMap.containsKey(it) }
                .flatMap {
                    val data = getNftsMap[Pair(it.first,it.second)]
                    walletNftRepository.deleteByNftIdAndWalletId(data!!.nftId,data.walletId)
                }
    }

}