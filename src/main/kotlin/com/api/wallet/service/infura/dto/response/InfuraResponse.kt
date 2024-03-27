package com.api.wallet.service.infura.dto.response

import java.math.BigDecimal
import java.math.BigInteger

data class InfuraResponse(
    val jsonrpc: String,
    val id: Int,
    val result: String,
) {
   companion object{
       fun InfuraResponse.toBigInteger(): BigInteger =
           BigInteger(this.result.removePrefix("0x"), 16)

       fun InfuraResponse.toBigDecimal(): BigDecimal =
           BigInteger(this.result.removePrefix("0x"), 16).toBigDecimal().divide(BigDecimal("1000000000000000000"))
   }
}

