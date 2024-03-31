package com.api.wallet.domain.walletNft.repository

import com.api.wallet.enums.NetworkType
import reactor.core.publisher.Flux

interface WalletNftRepositorySupport {

    fun findByWalletIdJoinNft(address: String, networkType: String) : Flux<WalletNftWithNft>

}
