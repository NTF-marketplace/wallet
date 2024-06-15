package com.api.wallet.domain.nft.repository

import com.api.wallet.domain.nft.Nft
import com.api.wallet.enums.ChainType
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface NftRepository : ReactiveCrudRepository<Nft,Long>, NftRepositorySupport {
    fun findAllByIdIn(ids: List<Long>) : Flux<Nft>
    fun findByTokenAddressAndChainTypeAndTokenId(address: String, chainType: ChainType,tokenId: String): Mono<Nft>
}