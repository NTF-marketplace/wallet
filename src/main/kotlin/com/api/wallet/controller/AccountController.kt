package com.api.wallet.controller

import com.api.wallet.controller.dto.request.DepositRequest
import com.api.wallet.controller.dto.request.WithdrawERC20Request
import com.api.wallet.controller.dto.request.WithdrawERC721Request
import com.api.wallet.controller.dto.response.AccountLogResponse
import com.api.wallet.controller.dto.response.AccountResponse
import com.api.wallet.controller.dto.response.NftMetadataResponse
import com.api.wallet.enums.AccountType
import com.api.wallet.enums.ChainType
import com.api.wallet.service.api.AccountLogService
import com.api.wallet.service.api.AccountService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@RestController
@RequestMapping("/v1/account")
class AccountController(
    private val accountService: AccountService,
    private val accountLogService: AccountLogService,
) {

    @GetMapping("auth/nft")
    fun getAccountNft(
        @RequestHeader("X-Auth-Address") address: String,
        @RequestParam(required = false) chainType: ChainType?,
        @PageableDefault(size = 50) pageable: Pageable,
    ): Mono<Page<NftMetadataResponse>> {
        return accountService.findByAccountNftByAddress(address,chainType,pageable)
    }

    @GetMapping("/has/nft")
    fun hasAccountNftByNft(
        @RequestParam address: String,
        @RequestParam nftId: Long,
    ): Mono<Boolean> {
        return accountService.checkAccountNftId(address, nftId)
    }

    @GetMapping("/has/balance")
    fun hasAccountBalance(
        @RequestParam address: String,
        @RequestParam chainType: ChainType,
        @RequestParam requiredBalance: BigDecimal
    ): Mono<Boolean> {
        return accountService.checkAccountBalance(address, chainType, requiredBalance)
    }

    @GetMapping("/auth")
    fun getAccount(
        @RequestHeader("X-Auth-Address") address: String,
        @RequestParam(required = false) chainType: ChainType?,
    ): Flux<AccountResponse> {
        return accountService.findByAccountsByAddress(address,chainType)
    }

    @GetMapping("/auth/logs")
    fun getAccountLogs(
        @RequestHeader("X-Auth-Address") address: String,
        @RequestParam(required = true) accountType: AccountType?,
        @PageableDefault(size = 50) pageable: Pageable,
    ): Mono<Page<AccountLogResponse>> {
        return accountLogService.findAllByAccountLog(address,accountType,pageable)
    }

    @PostMapping("/auth/deposit")
    fun depositAccount(
        @RequestHeader("X-Auth-Address") address: String,
        @RequestBody request: DepositRequest,
    ): Mono<Void> {
        return accountService.depositProcess(address,request)
    }

    @PostMapping("/auth/withdraw/ERC20")
    fun withdrawERC20Account(
        @RequestHeader("X-Auth-Address") address: String,
        @RequestBody request: WithdrawERC20Request,
    ): Mono<Void> {
        return accountService.withdrawERC20Process(address,request)
    }
    @PostMapping("/auth/withdraw/ERC721")
    fun withdrawERC721Account(
        @RequestHeader("X-Auth-Address") address: String,
        @RequestBody request: WithdrawERC721Request,
    ): Mono<Void> {
        return accountService.withdrawERC721Process(address,request)
    }

}