package com.api.wallet.domain.transaction.repository

import com.api.wallet.domain.transaction.Transaction
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface TransactionRepository: ReactiveCrudRepository<Transaction,Long> {

    fun findAllByWalletIdOrderByBlockTimestampDesc(address: String): Flux<Transaction>

}