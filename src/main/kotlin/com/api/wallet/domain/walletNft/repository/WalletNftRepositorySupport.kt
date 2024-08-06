package com.api.wallet.domain.walletNft.repository

import com.api.wallet.enums.ChainType
import reactor.core.publisher.Flux

interface WalletNftRepositorySupport {

    fun findByWalletIdJoinNft(address: String, chainType: ChainType) : Flux<WalletNftDto>

}
