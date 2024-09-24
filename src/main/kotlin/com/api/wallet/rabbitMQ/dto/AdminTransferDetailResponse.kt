package com.api.wallet.rabbitMQ.dto

import com.api.wallet.enums.TransferType
import java.math.BigDecimal

data class AdminTransferDetailResponse(
    val nftId: Long?,
    val transferType: TransferType,
    val balance: BigDecimal?,
)