package com.api.wallet.controller

import com.api.wallet.validator.SignatureValidator
import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.controller.dto.response.SignInResponse
import com.api.wallet.service.api.WalletService
import com.api.wallet.service.api.WalletTransactionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/v1/wallet")
class WalletController(
    private val walletService: WalletService,
) {

}