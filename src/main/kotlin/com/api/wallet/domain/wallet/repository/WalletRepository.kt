package com.api.wallet.domain.wallet.repository

import com.api.wallet.domain.wallet.Wallet
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface WalletRepository : ReactiveCrudRepository<Wallet,String>, WalletRepositorySupport {
    fun findByAddressAndNetworkType(address: String, networkType: String): Mono<Wallet>
    fun findAllByAddress(address: String): Flux<Wallet>
}