package com.api.wallet.domain.nft.repository

import com.api.wallet.domain.nft.Nft
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface NftRepository : ReactiveCrudRepository<Nft,Long>, NftRepositorySupport {

    fun findByTokenAddressAndNetworkTypeAndTokenId(address: String, networkType: String,tokenId: String): Mono<Nft>
}