package com.api.wallet.domain.account.detail

import com.api.wallet.enums.AccountType
import com.api.wallet.enums.TransferType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table
data class AccountDetailLog(
    @Id val id: Long? = null,
    val nftId: Long?,
    val balance: BigDecimal?,
    val transferType: TransferType
)
