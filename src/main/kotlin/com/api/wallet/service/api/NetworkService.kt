package com.api.wallet.service.api

import com.api.wallet.domain.network.Network
import com.api.wallet.domain.network.repository.NetworkRepository
import com.api.wallet.enums.NetworkType
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class NetworkService(
    private val networkRepository: NetworkRepository,
) {
    fun findByType(type: NetworkType): Mono<Network> {
       return networkRepository.findByType(type.toString())
            .switchIfEmpty(Mono.error(IllegalStateException("Network not found")))
    }
}