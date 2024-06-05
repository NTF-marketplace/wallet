package com.api.wallet.service.external.nft

import com.api.wallet.enums.ChainType
import com.api.wallet.properties.api.NftApiProperties
import com.api.wallet.service.external.moralis.dto.response.NFTResult
import com.api.wallet.service.external.nft.dto.NftResponse
import com.api.wallet.service.external.nft.dto.NftBatchRequest
import com.api.wallet.service.external.nft.dto.NftRequest
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class NftApiService(
    nftApiProperties: NftApiProperties,
) {

    private val webClient = WebClient.builder()
        .baseUrl(nftApiProperties.uri)
        .build()

    fun getNftBatch(requests: List<NftBatchRequest>): Flux<NftResponse> {
        return webClient.post()
            .uri("/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requests)
            .retrieve()
            .bodyToFlux(NftResponse::class.java)
    }

    fun saveNfts(requests: List<NFTResult>,chainType: ChainType): Flux<NftResponse> {
        return webClient.post()
            .uri {
                it.path("/save/${chainType}")
                it.build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requests)
            .retrieve()
            .bodyToFlux(NftResponse::class.java)
    }


    fun getNfts(requests: List<Long>) : Flux<NftResponse> {
        return webClient.get()
            .uri{
                it.queryParam("nftIds",requests)
                it.build()
            }
            .retrieve()
            .bodyToFlux(NftResponse::class.java)
    }

    fun getByWalletNft(wallet:String, chainType: ChainType): Flux<NftResponse> {
        return webClient.get()
            .uri{
                it.path("/wallet/${chainType}")
                    it.queryParam("wallet",wallet)
                    it.build()
            }
            .retrieve()
            .bodyToFlux(NftResponse::class.java)
    }

    fun getNftSave(requests: NftRequest): Mono<NftResponse> {
        return webClient.post()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requests)
            .retrieve()
            .bodyToMono(NftResponse::class.java)
    }

}

