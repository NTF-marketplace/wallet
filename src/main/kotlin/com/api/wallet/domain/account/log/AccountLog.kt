package com.api.wallet.domain.account.log

import com.api.wallet.domain.account.detail.AccountDetailLog
import com.api.wallet.enums.AccountType
import com.api.wallet.enums.TransaionStatusType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("account_log")
data class AccountLog(
    @Id val id : Long?= null,
    val accountId: Long,
    val accountDetailLogId: Long? = null,
    val createdAt: Long? = System.currentTimeMillis(),
    val accountType: AccountType,
    val transactionStatusType: TransaionStatusType = TransaionStatusType.PENDING
) {
    fun update(transactionStatusType: TransaionStatusType, accountDetailLog: AccountDetailLog?): AccountLog{
        return this.copy(transactionStatusType = transactionStatusType, accountDetailLogId = accountDetailLog?.id)
    }

}