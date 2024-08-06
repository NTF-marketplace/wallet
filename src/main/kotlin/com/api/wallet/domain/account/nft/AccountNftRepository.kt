package com.api.wallet.domain.account.nft

import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AccountNftRepository: ReactiveCrudRepository<AccountNft,Long>,AccountNftRepositorySupport {
    fun findByAccountId(accountId: Long): Flux<AccountNft>

    // fun countByAccountId(accountId: Long) : Mono<Long>
    fun findByAccountIdAndNftId(accountId: Long, nftId: Long): Mono<AccountNft>
}