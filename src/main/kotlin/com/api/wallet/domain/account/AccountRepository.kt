package com.api.wallet.domain.account

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface AccountRepository : ReactiveCrudRepository<Account,Long> {

    fun findAllByUserId(userId: Long): Flux<Account>
}