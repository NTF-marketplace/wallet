package com.api.wallet.rabbitMQ.dto

import com.api.wallet.enums.AccountType
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.TransferType
import java.math.BigDecimal

data class AdminTransferResponse(
    val id: Long,
    val walletAddress: String,
    val nftId: Long?,
    val timestamp: Long,
    val accountType: AccountType,
    val transferType: TransferType,
    val balance: BigDecimal?,
    val chainType: ChainType,
)
