package com.api.wallet.event

import com.api.wallet.enums.TransferType
import com.api.wallet.service.api.AccountLogService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AccountEventListener(
    private val accountLogService: AccountLogService,
) {
    @EventListener
    fun handleAccountEvent(event: AccountEvent): Mono<Void> {
        return accountLogService.saveAccountLog(event,TransferType.ERC20)
    }

    @EventListener
    fun handleAccountNftEvent(event: AccountNftEvent): Mono<Void> {
       return accountLogService.saveAccountNft(event,TransferType.ERC721)
    }
}