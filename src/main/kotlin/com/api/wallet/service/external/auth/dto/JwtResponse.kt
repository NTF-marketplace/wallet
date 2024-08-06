package com.api.wallet.service.external.auth.dto

data class JwtResponse(
    val accessToken: String,
    val refreshToken: String,
)