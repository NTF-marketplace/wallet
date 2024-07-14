package com.api.wallet.controller.dto.request

import com.api.wallet.enums.ChainType
import java.math.BigInteger

data class WithdrawERC20Request(
    val chainType: ChainType,
    val amount: BigInteger,
)
