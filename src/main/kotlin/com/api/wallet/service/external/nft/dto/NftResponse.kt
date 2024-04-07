package com.api.wallet.service.external.nft.dto

data class NftResponse(
    val id: Long,
    val tokenId: String,
    val tokenAddress: String,
    val chinType: String,
    val nftName: String,
    val collectionName: String,
    val image: String,
)
