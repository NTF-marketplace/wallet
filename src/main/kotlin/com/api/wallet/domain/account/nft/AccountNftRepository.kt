package com.api.wallet.domain.account.nft

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface AccountNftRepository: ReactiveCrudRepository<AccountNft,Long> {

    fun findByAccountId(accountId: Long): Flux<AccountNft>
}