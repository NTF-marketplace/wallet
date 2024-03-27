package com.api.wallet.service.api

import com.api.wallet.domain.nft.Nft
import com.api.wallet.domain.nft.repository.NftRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class NftService(
    private val nftRepository: NftRepository,
) {

    fun findByTokenAddress(tokenAddress: String,networkType: String): Mono<Nft> {
        return nftRepository.findByTokenAddressAndNetworkType(tokenAddress,networkType)
            .switchIfEmpty(
                nftRepository.insert(Nft(tokenAddress,networkType)
            ))
    }
}