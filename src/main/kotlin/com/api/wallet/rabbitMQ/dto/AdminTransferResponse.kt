package com.api.wallet.rabbitMQ.dto

import java.math.BigDecimal

data class AdminTransferResponse(
    val id: Long,
    val walletAddress: String,
    val nftId: Long?,
    val timestamp: Long,
    val accountType: String,
    val transferType: String,
    val balance: BigDecimal?,
)
