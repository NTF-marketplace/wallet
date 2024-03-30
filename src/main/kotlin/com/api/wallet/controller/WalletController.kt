package com.api.wallet.controller

import com.api.wallet.validator.SignatureValidator
import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.service.api.WalletService
import com.api.wallet.service.api.WalletTransactionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/v1/wallet")
class WalletController(
    private val walletService: WalletService,
    private val walletTransactionService: WalletTransactionService,
) {
    // 지갑 유효성 검증
    // 검증에 실패할 경우 return error
    // 검증에 성공한 경우 sign-up 또는, sign-in
    @PostMapping("/authenticate")
    fun signInOrSignUp(@RequestBody request: ValidateRequest): ResponseEntity<*> {
//        if(request.isAddressValid()){
//            signatureValidator.verifySignature(request)
//            return ResponseEntity.ok().body("Wallet authenticated with wallet address: $request")
//        }
        return ResponseEntity.badRequest().body("Wallet authenticated fail")
    }

//    @GetMapping("")
//    fun getTransactionsByWallet() {
//
//    }

}