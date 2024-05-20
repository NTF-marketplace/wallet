package com.api.wallet.domain.account.log

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("account_log")
class AccountLog(
    @Id val id : Long?= null,
    val userId: Long,
    val nftId: Long,
    val timestamp: Long,
    val accountType: String,
) {

}