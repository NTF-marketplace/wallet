package com.api.wallet.event

import com.api.wallet.domain.account.log.AccountLog
import com.api.wallet.domain.account.log.AccountLogRepository
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AccountEventListener(
    private val accountLogRepository: AccountLogRepository,
) {
    @EventListener
    fun handleAccountEvent(evnet: AccountEvent): Mono<Void> {
        val accountLog = AccountLog(
            id = null,
            accountId = evnet.account.id!!,
            nftId = null,
            accountType = evnet.accountType,
            timestamp = evnet.timestamp,
            balance = evnet.account.balance,
            transferType = "ERC20"
        )
        return accountLogRepository.save(accountLog).then()
    }

    @EventListener
    fun handleAccountNftEvent(evnet: AccountNftEvent): Mono<Void> {
        val accountLog = AccountLog(
            id = null,
            accountId = evnet.accountNft.accountId,
            nftId = evnet.accountNft.nftId,
            accountType = evnet.accountType,
            timestamp = evnet.timestamp,
            balance = null,
            transferType = "ERC721"
        )
        return accountLogRepository.save(accountLog).then()
    }
}