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
    fun handleAccountCreatedEvent(evnet: AccountCreatedEvent): Mono<Void> {
        val accountLog = AccountLog(
            id = null,
            userId = evnet.account.userId,
            nftId = evnet.account.nftId,
            accountType = evnet.accountType,
            timestamp = evnet.timestamp
        )
        return accountLogRepository.save(accountLog).then()
    }
}