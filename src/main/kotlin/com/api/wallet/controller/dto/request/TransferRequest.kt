package com.api.wallet.controller.dto.request

import com.api.wallet.enums.ChainType
import java.math.BigDecimal

data class TransferRequest(
    val fromAddress: String,
    val toAddress: String,
    val chainType: ChainType,
    val amount: BigDecimal,
    val nftId: Long,
)
