package com.api.wallet.rabbitMQ.dto

import com.api.wallet.enums.TokenType
import java.math.BigDecimal
data class ListingResponse(
    val id : Long,
    val nftId : Long,
    val address: String,
    val createdDateTime: Long,
    val endDateTime: Long,
    val active: Boolean,
    val price: BigDecimal,
    val tokenType: TokenType
)
