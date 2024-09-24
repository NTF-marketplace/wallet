package com.api.wallet.service.external.nft.dto

import com.api.wallet.enums.ChainType

data class NftResponse(
    val id: Long,
    val tokenId: String,
    val tokenAddress: String,
    val chainType: ChainType,
){

}