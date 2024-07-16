package com.api.wallet.domain.account.nft

import com.api.wallet.enums.ChainType
import reactor.core.publisher.Mono

interface AccountNftRepositorySupport {

    fun findByNftIdAndWalletAddressAndChainType(nftId: Long, address: String): Mono<AccountNft>
}