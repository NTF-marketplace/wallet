package com.api.wallet.service.external.nft

import com.api.wallet.controller.dto.response.NftMetadataResponse
import com.api.wallet.enums.ChainType
import com.api.wallet.properties.api.NftApiProperties
import com.api.wallet.service.external.nft.dto.NftResponse
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
        .baseUrl(nftApiProperties.uri )
        .build()
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

    fun getNftsByIds(nftIds: List<Long>): Flux<NftMetadataResponse> {
        return webClient.get()
            .retrieve()
            .bodyToFlux(NftMetadataResponse::class.java)
    }

    fun getNftById(nftId: Long): Mono<NftMetadataResponse> {
        return webClient.get()
            .uri{
                it.path("/${nftId}")
                it.build()
            }
            .retrieve()
            .bodyToMono(NftMetadataResponse::class.java)
    }


}

