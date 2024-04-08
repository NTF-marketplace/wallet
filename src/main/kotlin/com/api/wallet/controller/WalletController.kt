package com.api.wallet.controller

import com.api.wallet.validator.SignatureValidator
import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.controller.dto.response.SignInResponse
import com.api.wallet.controller.dto.response.TransactionResponse
import com.api.wallet.enums.NetworkType
import com.api.wallet.service.api.NftService
import com.api.wallet.service.api.WalletService
import com.api.wallet.service.api.WalletTransactionService
import com.api.wallet.service.external.nft.dto.NftResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/v1/wallet")
class WalletController(
    private val walletTransactionService: WalletTransactionService,
    private val nftService: NftService,
) {

    @GetMapping("/nft")
    fun readAllNftByWallet(
        @RequestParam networkType: NetworkType,
        @RequestParam walletAddress: String, // 토큰에서 식별자가져오기
        pageable: Pageable,
        ): Mono<ResponseEntity<Page<NftResponse>>> {
        return nftService.readAllNftByWallet(walletAddress,networkType,pageable)
            .flatMap { response ->
                Mono.just(ResponseEntity.ok().body(response))
            }.onErrorResume {
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null))
            }
    }

    @GetMapping("/transaction")
    fun readAllTransactions(
        @RequestParam networkType: NetworkType,
        @RequestParam walletAddress: String, //토큰에서 식별자가져오기
        pageable: Pageable,
    ): Mono<ResponseEntity<Page<TransactionResponse>>> {
        return walletTransactionService.readAllTransactions(walletAddress,networkType,pageable)
            .flatMap { response ->
                Mono.just(ResponseEntity.ok().body(response))
            }.onErrorResume {
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null))
            }
    }

}