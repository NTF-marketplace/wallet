package com.api.wallet.service.external.admin

import com.api.wallet.controller.dto.request.DepositRequest
import com.api.wallet.properties.api.AdminApiProperties
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class AdminApiService(
    adminApiProperties: AdminApiProperties
) {
    private val webClient = WebClient.builder()
        .baseUrl(adminApiProperties.uri)
        .build()

    fun createDeposit(address: String, request: DepositRequest): Mono<ResponseEntity<Void>> {
        return webClient.post()
            .uri {
                it.path("/deposit")
                it.queryParam("address", address)
                it.build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .toBodilessEntity()
    }

}