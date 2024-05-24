package com.api.wallet.domain.account

import com.api.wallet.domain.wallet.Wallet
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("account")
data class Account(
    @Id val id: Long? = null,
    val userId: Long,
    var balance: BigDecimal = BigDecimal.ZERO,
) {

    fun updateBalance(newBalance: BigDecimal): Account {
        return this.copy(balance = newBalance)
    }
}