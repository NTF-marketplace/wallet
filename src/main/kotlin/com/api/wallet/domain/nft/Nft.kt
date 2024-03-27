package com.api.wallet.domain.nft

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("nft")
class Nft(
    @Id val tokenAddress: String,
    val networkType: String
) {
}