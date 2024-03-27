package com.api.wallet.domain.nft.repository

import com.api.wallet.domain.nft.Nft
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface NftRepository : ReactiveCrudRepository<Nft,String>, NftRepositorySupport {
    fun findByTokenAddressAndNetworkType(address: String, networkType: String): Mono<Nft>
}