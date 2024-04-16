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
import com.api.wallet.service.external.nft.dto.NftBatchRequest
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
    private val moralisApiService: MoralisApiService,
    private val walletNftRepository: WalletNftRepository,
    private val nftApiService: NftApiService,
) {

    fun findOrCreateNft(tokenAddress: String,networkType: String,tokenId:String): Mono<Nft> {
        return nftRepository.findByTokenAddressAndNetworkTypeAndTokenId(tokenAddress,networkType,tokenId)
            .switchIfEmpty(
                nftRepository.insert(
                    Nft(
                    tokenId = tokenId, tokenAddress = tokenAddress, networkType = networkType)
                )
            )
    }

    @Transactional
    fun readAllNftByWallet(address: String, networkType:NetworkType?, pageable: Pageable): Mono<Page<NftResponse>> {
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
            .flatMap {
                pageableNfts(it,pageable)
            }
    }

    private fun pageableNfts(walletNfts: List<WalletNft>, pageable: Pageable): Mono<Page<NftResponse>> {
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(walletNfts.size)
        val pageContent = if (start <= end) walletNfts.subList(start, end) else listOf()

        val ids = pageContent.map { it.nftId }
        return nftRepository.findAllById(ids).collectList().flatMap { nfts->
            nftApiService.getNftBatch(toBatchRequest(nfts)).collectList()
        }.map { response ->
            // TODO(nft update하고 반환?)
            PageImpl(response, pageable, response.size.toLong())
        }
    }

    fun toBatchRequest(nfts: List<Nft>): List<NftBatchRequest> {
        return nfts.map { NftBatchRequest(it.tokenId, it.tokenAddress,it.networkType.convertNetworkTypeToChainType()) }
    }

    private fun getNftByWallet(wallet: Wallet): Flux<WalletNft> {
        val response = moralisApiService.getNFTsByAddress(wallet.address, wallet.networkType.convertNetworkTypeToChainType())
        val getNftsByWallet = walletNftRepository.findByWalletIdJoinNft(wallet.address,wallet.networkType)

        return Mono.zip(response, getNftsByWallet.collectList())
            .flatMapMany { tuple ->
                val responseNfts = tuple.t1.result.associateBy { Pair(it.tokenAddress,it.tokenId) }
                val getNfts = tuple.t2.associateBy { Pair(it.nftTokenAddress,it.nftTokenId) }

                deleteToWalletNft(responseNfts, getNfts, wallet)
                    .thenMany(addToWalletNft(responseNfts, getNfts, wallet))
                    .thenMany(walletNftRepository.findByWalletId(wallet.id!!))
            }
    }


    private fun addToWalletNft(
        responseNftsMap: Map<Pair<String,String>, NFTResult>,
        getNftsMap: Map<Pair<String,String>, WalletNftDto>,
        wallet: Wallet,
        ): Flux<WalletNft> {
          return Flux.fromIterable(responseNftsMap.keys).filter{ !getNftsMap.containsKey(it) }
                .flatMap {
                    val data = responseNftsMap[Pair(it.first,it.second)]
                    findOrCreateNft(data!!.tokenAddress,wallet.networkType,data!!.tokenId).flatMap { nft->
                         walletNftRepository.save(
                             WalletNft(
                                 walletId = wallet.id!!,
                                 nftId = nft.id!!,
                                 amount = data.amount!!.toInt()
                             )
                         )
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