package com.api.wallet.service.external.binance

import com.api.wallet.enums.TokenType
import com.api.wallet.properties.api.BinanceApiProperties
import com.api.wallet.service.external.binance.dto.BinanceTickerPriceResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class BinanceApiService(
    binanceApiProperties: BinanceApiProperties
) {

    private val webClient = WebClient.builder()
        .baseUrl(binanceApiProperties.uri)
        .build()


    fun getTickerPrice(tokenType: TokenType): Mono<BinanceTickerPriceResponse> {
        return webClient.get()
            .uri{
                it.path("/api/v3/ticker/price")
                it.queryParam("symbol","${tokenType}USDT")
                it.build()
            }
            .retrieve()
            .bodyToMono(BinanceTickerPriceResponse::class.java)
    }
}