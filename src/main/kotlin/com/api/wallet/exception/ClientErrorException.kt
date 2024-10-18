package com.api.wallet.exception


class ClientErrorException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
