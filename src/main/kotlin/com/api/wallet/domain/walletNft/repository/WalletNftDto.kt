package com.api.wallet.domain.walletNft.repository


data class WalletNftDto(
    val id: Long,
    val walletId: Long,
    val nftId: Long,
    val nftTokenAddress: String,
    val nftTokenId: String,
)
