package com.api.wallet.domain.account.nft

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("accountNft")
class AccountNft(
    @Id val id: Long? = null,
    val accountId: Long,
    var nftId: Long,
)