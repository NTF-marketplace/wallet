package com.api.wallet.controller

import com.api.wallet.controller.dto.request.DepositRequest
import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.controller.dto.response.AccountLogResponse
import com.api.wallet.controller.dto.response.AccountResponse
import com.api.wallet.controller.dto.response.NftMetadataResponse
import com.api.wallet.enums.AccountType
import com.api.wallet.enums.ChainType
import com.api.wallet.service.api.AccountLogService
import com.api.wallet.service.api.AccountService
import com.api.wallet.service.external.nft.dto.NftResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/account")
class AccountController(
    private val accountService: AccountService,
    private val accountLogService: AccountLogService,
) {

    // @GetMapping("/exist/nft")
    // fun existedAccountNft(
    //     @RequestParam nftId: Long,
    //     @RequestParam address: String,
    // ) {
    //     accountService.
    // }


    // @PostMapping("/deposit")
    // fun depositAccount(
    //     // @RequestParam address: String,
    //     @RequestBody request: DepositRequest
    // ) {
    //     //address는 따로 spring 시큐리티에서 토큰에 있는 값으로 꺼내올거임
    //     accountService.depositProcess()
    // }


    @GetMapping("/nft")
    fun getAccountNft(
        @RequestParam address: String,
        @RequestParam(required = false) chainType: ChainType?,
        @PageableDefault(size = 50) pageable: Pageable,
    ): Mono<Page<NftMetadataResponse>> {
        return accountService.findByAccountNftByAddress(address,chainType,pageable)
    }

    @GetMapping
    fun getAccount(
        @RequestParam address: String,
    ): Flux<AccountResponse> {
        return accountService.findByAccountsByAddress(address)
    }

    @GetMapping("/logs")
    fun getAccountLogs(
        @RequestParam address: String,
        @RequestParam(required = true) accountType: AccountType?,
        @PageableDefault(size = 50) pageable: Pageable,
    ): Mono<Page<AccountLogResponse>> {
        return accountLogService.findAllByAccountLog(address,accountType,pageable)
    }

}