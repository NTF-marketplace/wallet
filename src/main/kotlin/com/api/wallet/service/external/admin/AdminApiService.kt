package com.api.wallet.service.external.admin

import com.api.wallet.controller.dto.request.DepositRequest
import com.api.wallet.controller.dto.request.WithdrawERC20Request
import com.api.wallet.controller.dto.request.WithdrawERC721Request
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

    fun createDeposit(address: String, request: DepositRequest): Mono<Void> {
        return webClient.post()
            .uri {
                it.path("/deposit")
                it.queryParam("address", address)
                it.build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Void::class.java)
    }


    fun withdrawERC20(address: String, request:WithdrawERC20Request): Mono<ResponseEntity<Void>> {
        return webClient.post()
            .uri {
                it.path("/withdraw/erc20")
                it.queryParam("address", address)
                it.build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .toBodilessEntity()
    }


    fun withdrawERC721(address: String, request: WithdrawERC721Request): Mono<ResponseEntity<Void>> {
        return webClient.post()
            .uri {
                it.path("/withdraw/erc721")
                it.queryParam("address", address)
                it.build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .toBodilessEntity()
    }
}