package com.api.wallet.controller.dto.response

import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.service.external.auth.dto.JwtResponse

data class SignInResponse(
    val wallet: Wallet,
    val tokens: JwtResponse,
)
