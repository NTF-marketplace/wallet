package com.api.wallet.properties.api

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "admin")
data class AdminApiProperties (
    val uri: String
)