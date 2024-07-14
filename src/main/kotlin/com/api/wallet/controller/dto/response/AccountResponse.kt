package com.api.wallet.controller.dto.response

import com.api.wallet.domain.account.Account
import com.api.wallet.enums.ChainType
import java.math.BigDecimal

data class AccountResponse(
    val chainType: ChainType,
    val balance: Double,
    val balanceToUsdt: BigDecimal?
) {
     companion object{
         fun Account.toResponse(usdt: BigDecimal?,chainType: ChainType) = AccountResponse(
             balance = balance.toDouble(),
             balanceToUsdt = usdt?.let { it * balance },
             chainType = chainType
         )
     }
}
