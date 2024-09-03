package com.api.wallet.rabbitMQ.dto

import com.api.wallet.enums.AccountType
import com.api.wallet.enums.TransaionStatusType
import com.api.wallet.enums.TransferType

data class AdminTransferResponse(
    val accountLogId: Long,
    val accountType: AccountType,
    val transferType: TransferType,
    val transactionStatusType: TransaionStatusType,
    val adminTransferDetailResponse: AdminTransferDetailResponse?
)

