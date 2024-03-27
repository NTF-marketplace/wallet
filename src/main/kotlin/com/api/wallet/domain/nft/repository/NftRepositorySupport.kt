package com.api.wallet.domain.nft.repository

import com.api.wallet.domain.nft.Nft
import reactor.core.publisher.Mono

interface NftRepositorySupport {
    fun insert(nft: Nft) : Mono<Nft>
}