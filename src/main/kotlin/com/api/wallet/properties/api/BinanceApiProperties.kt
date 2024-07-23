package com.api.wallet.properties.api

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "binance")
data class BinanceApiProperties(
    val uri: String
)
