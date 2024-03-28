package com.api.wallet.domain.walletNft

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("wallet_nft")
class WalletNft(
    @Id val id: Long? = null,
    val walletId: String,
    val nftId: String
) {
}