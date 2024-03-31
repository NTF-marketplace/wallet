package com.api.wallet.domain.wallet

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("wallet")
data class Wallet(
    @Id val id: Long? = null,
    val address: String,
    val userId: Long,
    val networkType: String,
    var balance: BigDecimal,
    val createdAt: Long? = System.currentTimeMillis(),
    var updatedAt: Long? = System.currentTimeMillis()
){
    fun updateBalance(newBalance: BigDecimal): Wallet {
        return this.copy(balance = newBalance, updatedAt = System.currentTimeMillis())
    }
}