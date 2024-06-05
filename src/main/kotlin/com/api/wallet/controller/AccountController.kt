package com.api.wallet.controller

import com.api.wallet.domain.account.nft.AccountNft
import com.api.wallet.service.api.AccountService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("v1/wallet/account")
class AccountController(
    private val accountService: AccountService,
) {

    @GetMapping("/nft")
    fun getAccountNftByAddress(@RequestParam walletAddress: String): Flux<Long> {
        return accountService.findByAccountNftByAddress(walletAddress).map { it.nftId }
    }
}