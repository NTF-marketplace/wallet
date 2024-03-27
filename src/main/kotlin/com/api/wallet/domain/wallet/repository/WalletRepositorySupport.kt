package com.api.wallet.domain.wallet.repository

import com.api.wallet.domain.wallet.Wallet
import reactor.core.publisher.Mono

interface WalletRepositorySupport {
    fun insert(wallet: Wallet) : Mono<Wallet>
}