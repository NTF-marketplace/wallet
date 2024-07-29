package com.api.wallet.domain.account

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AccountRepository : ReactiveCrudRepository<Account,Long> {

    fun findByWalletId(walletId: Long): Mono<Account>
    fun findAllByWalletIdIn(walletIds: List<Long>): Flux<Account>
}