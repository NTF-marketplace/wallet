package com.api.wallet.controller.dto.response

import com.api.wallet.domain.account.Account
import java.math.BigDecimal

data class AccountResponse(
    val balance: Double,
    val balanceToUsdt: BigDecimal?
) {
     companion object{
         fun Account.toResponse(usdt: BigDecimal?) = AccountResponse(
             balance = balance.toDouble(),
             balanceToUsdt = usdt?.let { it * balance }

         )
     }
}
