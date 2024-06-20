package com.api.wallet.controller.dto.response

import java.math.BigDecimal

data class AccountLogResponse(
    val nftResponse: NftMetadataResponse?,
    val timestamp: Long,
    val accountType: String,
    val balance: BigDecimal
)
