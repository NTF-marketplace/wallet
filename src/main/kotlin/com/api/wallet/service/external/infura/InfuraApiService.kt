package com.api.wallet.service.external.infura

import com.api.wallet.enums.ChainType
import com.api.wallet.properties.api.key.ApiKeysProperties
import com.api.wallet.service.external.infura.dto.request.InfuraRequest
import com.api.wallet.service.external.infura.dto.response.InfuraResponse
import com.api.wallet.service.external.infura.dto.response.InfuraResponse.Companion.toBigDecimal
import com.api.wallet.service.external.infura.dto.response.InfuraResponse.Companion.toBigInteger
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.BigInteger

@Service
class InfuraApiService(
    private val apiKeysProperties: ApiKeysProperties,
) {

    private fun urlByChain(chainType: ChainType) : WebClient {
        val baseUrl = when (chainType) {
            ChainType.ETHEREUM_MAINNET -> "https://mainnet.infura.io"
            ChainType.POLYGON_MAINNET -> "https://polygon-mainnet.infura.io"
            ChainType.LINEA_MAINNET -> "https://linea-mainnet.infura.io"
            ChainType.LINEA_SEPOLIA -> "https://linea-sepolia.infura.io"
            ChainType.ETHEREUM_HOLESKY -> "https://polygon-mumbai.infura.io"
            ChainType.ETHEREUM_SEPOLIA -> "https://sepolia.infura.io"
            ChainType.POLYGON_AMOY -> "https://polygon-amoy.infura.io"
        }
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build()
    }


    fun getBlockNumber(chainType: ChainType): Mono<BigInteger> {
        val requestBody = InfuraRequest(method = "eth_blockNumber")
        val webClient = urlByChain(chainType)

        return webClient.post()
            .uri("/v3/${apiKeysProperties.infura}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(InfuraResponse::class.java)
            .mapNotNull { it.toBigInteger() }
    }

    fun getBalance(walletAddress: String, chainType: ChainType) : Mono<BigDecimal> {
        val webClient = urlByChain(chainType)
        val requestBody = InfuraRequest(method = "eth_getBalance", params = listOf(walletAddress,"latest"))

        return webClient.post()
            .uri("/v3/${apiKeysProperties.infura}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(InfuraResponse::class.java)
            .mapNotNull { it.toBigDecimal() }
    }

}