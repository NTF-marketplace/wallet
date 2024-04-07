package com.api.wallet.service.external.nft

import com.api.wallet.properties.api.NftApiProperties
import com.api.wallet.service.external.nft.dto.NftResponse
import com.api.wallet.service.external.nft.dto.NftBatchRequest
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Service
class NftApiService(
    nftApiProperties: NftApiProperties,
) {

    private val webClient = WebClient.builder()
        .baseUrl(nftApiProperties.uri ?: throw IllegalAccessException("must be nft-url"))
        .build()

    fun getNftBatch(requests: List<NftBatchRequest>): Flux<NftResponse> {
        return webClient.post()
            .uri("/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requests)
            .retrieve()
            .bodyToFlux(NftResponse::class.java)
    }

}

