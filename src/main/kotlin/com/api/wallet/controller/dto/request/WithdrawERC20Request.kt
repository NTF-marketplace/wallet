package com.api.wallet.controller.dto.request

import com.api.wallet.enums.ChainType
import java.math.BigDecimal

data class WithdrawERC20Request(
    val chainType: ChainType,
    val amount: BigDecimal,
    val accountLogId: Long?,
)
