package com.api.wallet.controller

import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.controller.dto.response.SignInResponse
import com.api.wallet.service.api.WalletService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1")
class AuthController(
    private val walletService: WalletService,
) {
    @PostMapping("/signIn")
    fun signInOrSignUp(@RequestBody request: ValidateRequest): Mono<ResponseEntity<SignInResponse>> {
        return walletService.signInOrSignUp(request)
            .flatMap { signInResponse ->
                Mono.just(ResponseEntity.ok().body(signInResponse))
            }
            .onErrorResume {
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null))
            }
    }
}