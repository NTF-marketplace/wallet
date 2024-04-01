package com.api.wallet.service.external.auth

import com.api.wallet.enums.ChainType
import com.api.wallet.service.external.auth.dto.JwtRequest
import com.api.wallet.service.external.auth.dto.JwtResponse
import com.api.wallet.service.external.infura.InfuraApiService
import com.api.wallet.service.external.infura.dto.request.InfuraRequest
import com.api.wallet.service.external.infura.dto.response.InfuraResponse
import com.api.wallet.service.external.infura.dto.response.InfuraResponse.Companion.toBigInteger
import com.api.wallet.service.external.moralis.MoralisService
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigInteger

@Service
class AuthApiService {
    private val webClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build()


    fun getJwtToken(request: String): Mono<JwtResponse> {
        val requestBody = JwtRequest(request)
        return webClient.post()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(JwtResponse::class.java)
    }

    companion object {
        private val baseUrl = "http://localhost:8081/v1/auth"
    }

}