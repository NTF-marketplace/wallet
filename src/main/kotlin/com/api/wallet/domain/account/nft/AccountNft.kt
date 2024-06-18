package com.api.wallet.domain.account.nft

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("account_nft")
class AccountNft(
    @Id val id: Long? = null,
    val accountId: Long,
    var nftId: Long,
)