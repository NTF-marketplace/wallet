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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/v1/wallet")
class WalletController(
    private val walletTransactionService: WalletTransactionService,
    private val nftService: NftService,
) {

    // response 공용화
    @GetMapping("/nft")
    fun readAllNftByWallet(
        @RequestParam networkType: NetworkType?,
        @RequestParam walletAddress: String
    ): Mono<ResponseEntity<List<NftResponse>>> {
        return nftService.readAllNftByWallet(walletAddress, networkType)
            .collectList()
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorResume { e ->
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
            }
    }




}