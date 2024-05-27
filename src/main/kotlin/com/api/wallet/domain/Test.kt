package com.api.wallet.domain

import com.api.wallet.enums.MyEnum
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("test")
data class Test(
    @Id val id: Long? = null,
    val type: MyEnum
) {

}