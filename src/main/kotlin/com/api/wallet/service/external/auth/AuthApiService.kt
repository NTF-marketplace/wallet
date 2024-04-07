package com.api.wallet.service.external.auth

import com.api.wallet.properties.api.AuthApiProperties
import com.api.wallet.service.external.auth.dto.JwtRequest
import com.api.wallet.service.external.auth.dto.JwtResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class AuthApiService(
    authApiProperties: AuthApiProperties,
) {
    private val webClient = WebClient.builder()
        .baseUrl(authApiProperties.uri ?: throw IllegalAccessException("must be auth-url"))
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