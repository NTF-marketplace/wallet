package com.api.wallet.domain.walletNft.repository

import io.r2dbc.spi.Row
import java.math.BigDecimal


data class WalletNftDto(
    val id: Long,
    val walletId: Long,
    val nftId: Long,
) {
    companion object {
        fun fromRow(row: Row): WalletNftDto {
            return WalletNftDto(
                id = (row.get("wn_id") as Number).toLong(),
                walletId = (row.get("wallet_address") as Number).toLong(),
                nftId = (row.get("nft_id") as Number).toLong(),
            )
        }
    }
}
