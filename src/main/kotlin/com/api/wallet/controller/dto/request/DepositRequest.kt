package com.api.wallet.controller.dto.request

import com.api.wallet.enums.ChainType

data class DepositRequest(
    val chainType: ChainType,
    val transactionHash: String,
    val accountLogId: Long?,
)
