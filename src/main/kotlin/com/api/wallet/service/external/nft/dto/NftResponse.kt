package com.api.wallet.service.external.nft.dto

import com.api.wallet.domain.nft.Nft
import com.api.wallet.enums.ChainType

data class NftResponse(
    val id: Long,
    val tokenId: String,
    val tokenAddress: String,
    val chainType: ChainType,
){
    companion object{
        fun NftResponse.toEntity() = Nft(
            id = this.id,
            tokenId = this.tokenId,
            tokenAddress = this.tokenAddress,
            chainType = this.chainType
        )
    }
}