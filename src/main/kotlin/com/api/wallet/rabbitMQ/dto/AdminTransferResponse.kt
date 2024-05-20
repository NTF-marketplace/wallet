package com.api.wallet.rabbitMQ.dto

data class AdminTransferResponse(
    val id: Long,
    val walletAddress: String,
    val nftId: Long,
    val timestamp: Long,
    val accountType: String
)
