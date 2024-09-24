package com.api.wallet.kafka.dto

import com.api.wallet.enums.ChainType
import com.api.wallet.enums.OrderType
import com.api.wallet.enums.StatusType
import java.math.BigDecimal

data class SaleResponse(
    val id : Long,
    val nftId : Long,
    val address: String,
    val createdDateTime: Long,
    val endDateTime: Long,
    val statusType: StatusType,
    val price: BigDecimal,
    val chainType: ChainType,
    val orderType: OrderType
)

