package com.api.wallet.service.external.binance.dto

import java.math.BigDecimal

data class BinanceTickerPriceResponse(
    val symbol : String,
    val price: BigDecimal,
)

