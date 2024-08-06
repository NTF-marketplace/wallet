package com.api.wallet.controller.dto.response

import java.math.BigDecimal

data class WalletAccountResponse(
    val balance : BigDecimal,
    val account : AccountResponse
)
