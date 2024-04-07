package com.api.wallet.properties.api

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth")
class AuthApiProperties {
    var uri: String? = null

}