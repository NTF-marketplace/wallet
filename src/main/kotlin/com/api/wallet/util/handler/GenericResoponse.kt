package com.api.wallet.util.handler

data class ResponseWrapper<T>(
    val data: T?,
    val error: String? = null
)
