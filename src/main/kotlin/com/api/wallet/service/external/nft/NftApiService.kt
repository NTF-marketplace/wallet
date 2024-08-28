package com.api.wallet.service.external.nft

import com.api.wallet.controller.dto.response.NftMetadataResponse
import com.api.wallet.enums.ChainType
import com.api.wallet.properties.api.NftApiProperties
import com.api.wallet.service.external.nft.dto.NftResponse
import com.api.wallet.service.external.nft.dto.NftRequest
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator
import io.github.resilience4j.reactor.retry.RetryOperator
import io.github.resilience4j.retry.Retry
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class NftApiService(
    nftApiProperties: NftApiProperties,
    circuitBreakerRegistry: CircuitBreakerRegistry
) {

    private val circuitBreaker: io.github.resilience4j.circuitbreaker.CircuitBreaker = circuitBreakerRegistry.circuitBreaker("nftService")
    private val retry: Retry = Retry.ofDefaults("nftServiceRetry")


    private val webClient = WebClient.builder()
        .baseUrl(nftApiProperties.uri )
        .build()


    fun getByWalletNft(wallet: String, chainType: ChainType): Flux<NftResponse> {
        return webClient.get()
            .uri {
                it.path("/wallet/${chainType}")
                    .queryParam("wallet", wallet)
                    .build()
            }
            .retrieve()
            .bodyToFlux(NftResponse::class.java)
            .transformDeferred(RetryOperator.of(retry))
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .doOnError { throwable ->
                println("Error occurred: ${throwable.message}")
            }
            .onErrorResume { throwable ->
                println("CircuitBreaker fallback: ${throwable.message}")
                Flux.error(throwable)
            }
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

