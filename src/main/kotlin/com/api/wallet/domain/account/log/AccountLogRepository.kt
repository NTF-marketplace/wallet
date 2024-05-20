package com.api.wallet.domain.account.log

import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface AccountLogRepository : ReactiveCrudRepository<AccountLog,Long> {
    fun findAllByUserIdAndAccountType(userId: Long, accountType: String, pageable: Pageable): Flux<AccountLog>

    fun findAllByUserId(userId: Long,pageable: Pageable) : Flux<AccountLog>
}