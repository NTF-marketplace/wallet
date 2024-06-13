package com.api.wallet.controller

import com.api.wallet.controller.dto.response.AccountResponse
import com.api.wallet.enums.ChainType
import com.api.wallet.service.api.AccountService
import com.api.wallet.service.external.nft.dto.NftResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/account")
class AccountController(
    private val accountService: AccountService,
) {

    @GetMapping("/nft")
    fun getAccountNft(
        @RequestParam address: String,
        @RequestParam(required = false) chainType: ChainType?,
        @PageableDefault(size = 50) pageable: Pageable,
    ): Mono<ResponseEntity<Page<NftResponse>>> {
        return accountService.findByAccountNftByAddress(address,chainType,pageable)
            .map { ResponseEntity.ok(it) }
            .onErrorResume {
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
            }
    }

    @GetMapping
    fun getAccount(
        @RequestParam address: String,
    ): Mono<ResponseEntity<Flux<AccountResponse>>> {
        return accountService.findByAccountsByAddress(address)
            .collectList()
            .flatMap {
                Mono.just(ResponseEntity.ok(Flux.fromIterable(it)))
            }
            .onErrorResume {
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
            }

    }

    // @GetMapping
    // fun getAccountLog(
    //     @RequestParam address: String,
    //     @PageableDefault(size = 50) pageable: Pageable,
    // )
}