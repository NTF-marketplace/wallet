package com.api.wallet.controller

import com.api.wallet.controller.dto.request.DepositRequest
import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.controller.dto.request.WithdrawERC20Request
import com.api.wallet.controller.dto.request.WithdrawERC721Request
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
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
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
    @GetMapping("/nft")
    fun getAccountNft(
        @RequestParam address: String,
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

    @GetMapping
    fun getAccount(
        @RequestParam address: String,
        @RequestParam(required = false) chainType: ChainType?,
    ): Flux<AccountResponse> {
        return accountService.findByAccountsByAddress(address,chainType)
    }

    @GetMapping("/logs")
    fun getAccountLogs(
        @RequestParam address: String,
        @RequestParam(required = true) accountType: AccountType?,
        @PageableDefault(size = 50) pageable: Pageable,
    ): Mono<Page<AccountLogResponse>> {
        return accountLogService.findAllByAccountLog(address,accountType,pageable)
    }


    @PostMapping("/deposit")
    fun depositAccount(
        @RequestParam address: String,
        @RequestBody request: DepositRequest,
    ): Mono<ResponseEntity<Void>> {
        return accountService.depositProcess(address,request)
    }


    @PostMapping("/withdraw/ERC20")
    fun withdrawERC20Account(
        @RequestParam address: String,
        @RequestBody request: WithdrawERC20Request,
    ): Mono<ResponseEntity<Void>> {
        return accountService.withdrawERC20Process(address,request)
    }

    @PostMapping("/withdraw/ERC721")
    fun withdrawERC721Account(
        @RequestParam address: String,
        @RequestBody request: WithdrawERC721Request,
    ): Mono<ResponseEntity<Void>> {
        return accountService.withdrawERC721Process(address,request)
    }

}