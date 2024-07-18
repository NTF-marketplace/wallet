package com.api.wallet.util.handler

data class ResponseWrapper<T>(
    val data: T? = null,
    val error: String? = null
)
