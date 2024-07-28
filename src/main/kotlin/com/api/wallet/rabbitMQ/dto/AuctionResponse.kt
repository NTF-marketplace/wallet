package com.api.wallet.rabbitMQ.dto

import com.api.wallet.enums.StatusType
import com.api.wallet.enums.TokenType
import java.math.BigDecimal

data class AuctionResponse(
    val id : Long,
    val nftId : Long,
    val address: String,
    val createdDateTime: Long,
    val endDateTime: Long,
    val statusType: StatusType,
    val startingPrice: BigDecimal,
    val tokenType: TokenType
)

