package com.api.wallet.controller

import com.api.wallet.controller.dto.response.WalletAccountResponse
import com.api.wallet.enums.ChainType
import com.api.wallet.service.api.NftService
import com.api.wallet.service.api.WalletService
import com.api.wallet.service.external.nft.dto.NftResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/v1/wallet")
class WalletController(
    private val nftService: NftService,
    private val walletService: WalletService,
) {

    // response 공용화
    //  api 게이트웨이에서 헤더전파
    @GetMapping("/nft")
    fun getAllNft(
        @RequestParam(required = false) chainType: ChainType?,
        @RequestParam address: String,
        @PageableDefault(size = 50) pageable: Pageable,
    ): Mono<ResponseEntity<Page<NftResponse>>> {
        return nftService.readAllNftByWallet(address, chainType,pageable)
            .map { ResponseEntity.ok(it) }
            .onErrorResume {
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
            }
    }

    @GetMapping("/{chainType}")
    fun getBalance(@PathVariable chainType: ChainType,@RequestParam address: String)
    : Mono<ResponseEntity<WalletAccountResponse>> {
        return walletService.getWalletAccount(address,chainType).map { ResponseEntity.ok(it) }
            .onErrorResume {
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
            }
    }

}