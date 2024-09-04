package com.api.wallet.domain.account.detail

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface AccountDetailLogRepository : ReactiveCrudRepository<AccountDetailLog, Long> {
    fun findAllByIdIn(accountIds: List<Long>): Flux<AccountDetailLog>
}