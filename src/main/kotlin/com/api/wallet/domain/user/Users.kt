package com.api.wallet.domain.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class Users(
    @Id val id: Long? = null,
    val nickName: String
)