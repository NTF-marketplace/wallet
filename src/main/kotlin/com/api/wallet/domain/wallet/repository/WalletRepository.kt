package com.api.wallet.domain.wallet.repository

import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.enums.ChainType
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface WalletRepository : ReactiveCrudRepository<Wallet,Long>, WalletRepositorySupport {
    fun findByAddressAndChainType(address: String, chainType: ChainType): Mono<Wallet>
    fun findAllByAddress(address: String): Flux<Wallet>

    fun findByChainType(chainType: ChainType): Flux<Wallet>
}