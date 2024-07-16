package com.api.wallet.domain.account


import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("account")
data class Account(
    @Id val id: Long? = null,
    val walletId: Long,
    var balance: BigDecimal = BigDecimal.ZERO,
) {

    // fun updateBalance(newBalance: BigDecimal): Account {
    //     return this.copy(balance = newBalance)
    // }

    fun deposit(amount: BigDecimal): Account {
        return this.copy(balance = this.balance.add(amount))
    }

    fun withdraw(amount: BigDecimal): Account {
        return this.copy(balance = this.balance.subtract(amount))
    }
}