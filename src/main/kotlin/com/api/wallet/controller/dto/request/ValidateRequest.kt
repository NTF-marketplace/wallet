package com.api.wallet.controller.dto.request

import com.api.wallet.enums.NetworkType


data class ValidateRequest(
    val address: String,
    val message: String,
    val signature: String,
    val network: NetworkType,
) {
    companion object {
        fun String.isValidEthereumAddress(): Boolean {
            val addressPattern = "^0x[a-fA-F0-9]{40}$".toRegex()
            return this.matches(addressPattern)
        }
    }
    fun isAddressValid(): Boolean = address.isValidEthereumAddress()
}


