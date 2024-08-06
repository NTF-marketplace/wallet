package com.api.wallet.properties.api.key

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "apikey")
data class ApiKeysProperties(
    val infura: String,
)
