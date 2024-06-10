package com.api.wallet.domain.account.nft

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface AccountNftRepository: ReactiveCrudRepository<AccountNft,Long> {
    fun findByAccountId(accountId: Long): Mono<AccountNft>
}