package com.api.wallet.controller.dto.response

import com.api.wallet.enums.TransaionStatusType
import com.api.wallet.enums.TransferType
import java.math.BigDecimal

data class AccountLogResponse(
    val timestamp: Long,
    val accountType: String,
    val transactionStatusType: TransaionStatusType,
    val detail: AccountLogDetailResponse?
)

data class AccountLogDetailResponse(
    val nftResponse: NftMetadataResponse?,
    val balance: BigDecimal,
    val transferType: TransferType
)
