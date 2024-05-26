package com.api.wallet.domain.walletNft.repository

import com.api.wallet.domain.walletNft.WalletNft
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface WalletNftRepository : ReactiveCrudRepository<WalletNft,Long>, WalletNftRepositorySupport {

    fun findByWalletId(walletId: Long): Flux<WalletNft>

    fun deleteByNftIdAndWalletId(nftId: Long,walletId: Long): Mono<Void>
}