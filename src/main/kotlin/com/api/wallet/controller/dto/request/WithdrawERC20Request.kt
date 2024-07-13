package com.api.wallet.controller.dto.request

import java.math.BigDecimal

data class WithdrawERC20Request(
    val amount: BigDecimal,
)
