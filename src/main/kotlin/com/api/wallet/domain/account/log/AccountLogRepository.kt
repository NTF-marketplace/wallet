package com.api.wallet.domain.account.log

import com.api.wallet.enums.AccountType
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


interface AccountLogRepository : ReactiveCrudRepository<AccountLog,Long> {
    fun findByAccountId(accountId:Long):Mono<AccountLog>

    fun findByAccountIdInAndAccountTypeOrderByCreatedAtDesc(ids:List<Long>,accountType: AccountType,pageable: Pageable): Flux<AccountLog>

    fun countByAccountIdInAndAccountType(ids:List<Long>, accountType: AccountType) : Mono<Long>

    fun findByAccountIdInOrderByCreatedAtDesc(ids:List<Long>,pageable: Pageable) : Flux<AccountLog>
    fun countByAccountIdIn(ids:List<Long>): Mono<Long>

}