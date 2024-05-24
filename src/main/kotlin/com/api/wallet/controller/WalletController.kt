package com.api.wallet.controller

import com.api.wallet.enums.NetworkType
import com.api.wallet.service.api.NftService
import com.api.wallet.service.external.nft.dto.NftResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/v1/wallet")
class WalletController(
    private val nftService: NftService,
) {

    // response 공용화
    //  api 게이트웨이에서 헤더전파
    @GetMapping("/nft")
    fun readAllNftByWallet(
        @RequestParam networkType: NetworkType?,
        @RequestParam walletAddress: String
    ): Mono<ResponseEntity<List<NftResponse>>> {
        return nftService.readAllNftByWallet(walletAddress, networkType)
            .collectList()
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .onErrorResume {
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
            }
    }

}