package com.api.wallet.domain.transaction

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("transaction")
class Transaction(
    @Id val id: Long? = null,
    val nftId: String, // 외래키
    val toAddress: String,
    val fromAddress: String,
    val amount: Int,
    val value: BigDecimal,
    val hash: String?,
    val blockTimestamp: Long?,
    val walletId: String?, // 외래키
){
}