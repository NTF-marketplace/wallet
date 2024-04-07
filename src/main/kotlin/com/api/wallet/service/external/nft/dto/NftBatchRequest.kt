package com.api.wallet.service.external.nft.dto

import com.api.wallet.enums.ChainType

data class NftBatchRequest(
    val tokenId: String,
    val tokenAddress: String,
    val chainType: ChainType,
)
