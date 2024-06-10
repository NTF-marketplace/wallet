package com.api.wallet.controller

import com.api.wallet.controller.dto.response.AccountResponse
import com.api.wallet.domain.account.nft.AccountNft
import com.api.wallet.enums.ChainType
import com.api.wallet.service.api.AccountService
import com.api.wallet.service.external.nft.dto.NftResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/v1/wallet/account")
class AccountController(
    private val accountService: AccountService,
) {

    @GetMapping("/nft")
    fun getAccountNftByAddress(
        @RequestParam walletAddress: String,
        @RequestParam chainType: ChainType?,
    ): Flux<NftResponse> {
        return accountService.findByAccountNftByAddress(walletAddress,chainType)
    }

    @GetMapping
    fun getAccountByAddress(
        @RequestParam walletAddress: String,
        @RequestParam chainType: ChainType?,
    ): Flux<AccountResponse> {
        return accountService.findByAccountByAddress(walletAddress,chainType)
    }
}