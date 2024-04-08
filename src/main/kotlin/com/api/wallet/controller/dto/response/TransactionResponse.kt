package com.api.wallet.controller.dto.response

import com.api.wallet.service.external.nft.dto.NftResponse
import java.math.BigDecimal

data class TransactionResponse(
    val toAddress: String,
    val fromAddress: String,
    val amount: Int,
    val value: BigDecimal,
    val blockTimestamp: Long?,
    val walletId: Long?,
    val nft: NftResponse
)
