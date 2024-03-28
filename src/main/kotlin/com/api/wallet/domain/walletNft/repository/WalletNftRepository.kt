package com.api.wallet.domain.walletNft.repository

import com.api.wallet.domain.walletNft.WalletNft
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface WalletNftRepository : ReactiveCrudRepository<WalletNft,Long> {

    fun findByWalletId(address: String): Flux<WalletNft>

    fun deleteByNftIdAndWalletId(tokenAddress: String,walletAddress: String): Mono<Void>
}