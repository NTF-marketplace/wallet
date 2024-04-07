package com.api.wallet.controller

import com.api.wallet.validator.SignatureValidator
import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.controller.dto.response.SignInResponse
import com.api.wallet.enums.NetworkType
import com.api.wallet.service.api.NftService
import com.api.wallet.service.api.WalletService
import com.api.wallet.service.api.WalletTransactionService
import com.api.wallet.service.external.nft.dto.NftResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/v1/wallet")
class WalletController(
    private val walletService: WalletService,
    private val nftService: NftService,
) {

    @GetMapping("/nft")
    fun readAllNftByWallet(
        @PathVariable networkType: NetworkType,
        walletAddress: String,
        pageable: Pageable,
        ): Mono<ResponseEntity<Page<NftResponse>>> {
        return nftService.readAllNftByWallet(walletAddress,networkType,pageable)
            .flatMap { response ->
                Mono.just(ResponseEntity.ok().body(response))
            }
            .onErrorResume {
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null))
            }
    }

}