package com.api.wallet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@ConfigurationPropertiesScan
class WalletApplication

fun main(args: Array<String>) {
    runApplication<WalletApplication>(*args)
}
