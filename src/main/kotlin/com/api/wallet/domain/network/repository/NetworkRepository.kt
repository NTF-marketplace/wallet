package com.api.wallet.domain.network.repository

import com.api.wallet.domain.network.Network
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface NetworkRepository: ReactiveCrudRepository<Network,Long> {

    fun findByType(type: String): Mono<Network>
}