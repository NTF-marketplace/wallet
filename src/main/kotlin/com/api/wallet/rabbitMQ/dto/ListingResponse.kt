package com.api.wallet.rabbitMQ.dto

import com.api.wallet.enums.ChainType
import com.api.wallet.enums.StatusType
import java.math.BigDecimal
data class ListingResponse(
    val id : Long,
    val nftId : Long,
    val address: String,
    val createdDateTime: Long,
    val endDateTime: Long,
    val statusType: StatusType,
    val price: BigDecimal,
    val chainType: ChainType
)
