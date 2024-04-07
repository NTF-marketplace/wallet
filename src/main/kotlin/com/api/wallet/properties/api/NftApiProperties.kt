package com.api.wallet.properties.api

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nft")
class NftApiProperties {
    var uri: String? = null
}