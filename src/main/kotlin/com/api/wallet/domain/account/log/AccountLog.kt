package com.api.wallet.domain.account.log

import com.api.wallet.enums.AccountType
import com.api.wallet.enums.TransferType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("account_log")
data class AccountLog(
    @Id val id : Long?= null,
    val accountId: Long,
    val nftId: Long?,
    val timestamp: Long,
    val accountType: AccountType,
    val balance: BigDecimal?,
    val transferType: TransferType
) {

}