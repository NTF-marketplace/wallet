package com.api.wallet.domain.transaction

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("transaction")
class Transaction(
    @Id val id: Long? = null,
    val nftId: String,
    val toAddress: String,
    val fromAddress: String,
    val amount: Int, // 수량
    val value: BigDecimal, // 가치
    val hash: String?,
    val blockTimestamp: Long?,
    val walletId: String?,
){
}