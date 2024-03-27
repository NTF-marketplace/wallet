package com.api.wallet.domain.wallet.repository

import com.api.wallet.domain.wallet.Wallet
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import reactor.core.publisher.Mono

class WalletRepositorySupportImpl(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
): WalletRepositorySupport {
    override fun insert(wallet: Wallet): Mono<Wallet> {
        return r2dbcEntityTemplate.insert(Wallet::class.java).using(wallet)
    }
}