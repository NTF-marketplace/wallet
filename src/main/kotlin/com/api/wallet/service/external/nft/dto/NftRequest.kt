package com.api.wallet.service.external.nft.dto

import com.api.wallet.enums.ChainType

data class NftRequest(
    val tokenAddress: String,
    val tokenId: String,
    val chainType: ChainType
)
