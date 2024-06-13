package com.api.wallet.domain.account.nft

import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AccountNftRepository: ReactiveCrudRepository<AccountNft,Long> {
    fun findByAccountId(accountId: Long): Flux<AccountNft>

    fun countByAccountId(accountId: Long) : Mono<Long>
}