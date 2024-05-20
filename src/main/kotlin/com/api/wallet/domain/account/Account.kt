package com.api.wallet.domain.account

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("account")
class Account(
    @Id val id: Long? = null,
    val userId: Long,
    val nftId: Long,
) {
}