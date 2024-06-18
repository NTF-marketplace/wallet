package com.api.wallet.controller.dto.response

import com.api.wallet.service.external.nft.dto.NftResponse
import java.math.BigDecimal

data class AccountLogResponse(
    val nftResponse: NftResponse?,
    val timestamp: Long,
    val accountType: String,
    val balance: BigDecimal
)
