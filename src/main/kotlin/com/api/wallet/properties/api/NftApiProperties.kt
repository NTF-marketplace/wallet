package com.api.wallet.properties.api

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nft")
data class NftApiProperties(
    val uri: String
)