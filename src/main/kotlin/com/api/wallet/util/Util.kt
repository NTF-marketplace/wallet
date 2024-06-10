package com.api.wallet.util

import com.api.wallet.enums.ChainType
import com.api.wallet.enums.TokenType
import com.google.gson.Gson
import java.time.Instant
import java.time.format.DateTimeFormatter

object Util {

    inline fun <reified T> fromJson(json: String): T = Gson().fromJson(json, T::class.java)

    fun timestampToString(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        return DateTimeFormatter.ISO_INSTANT.format(instant)
    }

    fun Long.toIsoString(): String = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(this))

    fun String.convertNetworkTypeToChainType(): ChainType {
        return when (this) {
            "ETHEREUM" -> ChainType.ETHEREUM_MAINNET
            "POLYGON" -> ChainType.POLYGON_MAINNET
            else -> throw IllegalArgumentException("Unknown network type: $this")
        }
    }

    fun String.toTimestamp() = Instant.parse(this).toEpochMilli()

    fun ChainType.toTokenType(): TokenType {
        return when (this) {
            ChainType.ETHEREUM_MAINNET -> TokenType.ETH
            ChainType.POLYGON_MAINNET -> TokenType.MATIC
            ChainType.POLYGON_AMOY -> TokenType.MATIC
            ChainType.ETHEREUM_SEPOLIA -> TokenType.ETH
            ChainType.ETHEREUM_HOLESKY -> TokenType.ETH
            else -> throw IllegalArgumentException("Unknown ChainType: $this")
        }
    }
}