package com.api.wallet.domain.walletNft.repository


data class WalletNftWithNft(
    val id: Long,
    val walletId: Long,
    val nftId: Long,
    val nftTokenAddress: String,
    val nftTokenId: String,
)
