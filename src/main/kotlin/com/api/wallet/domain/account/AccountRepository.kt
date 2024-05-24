package com.api.wallet.domain.account

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AccountRepository : ReactiveCrudRepository<Account,Long> {

    fun findByUserId(userId: Long): Mono<Account>
}