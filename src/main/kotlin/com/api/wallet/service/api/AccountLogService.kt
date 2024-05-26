package com.api.wallet.service.api

import com.api.wallet.domain.account.log.AccountLog
import com.api.wallet.domain.account.log.AccountLogRepository
import com.api.wallet.enums.TransferType
import com.api.wallet.event.AccountEvent
import com.api.wallet.event.AccountNftEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AccountLogService(
    private val accountLogRepository: AccountLogRepository
) {

    fun saveAccountLog(event: AccountEvent,transferType: TransferType) : Mono<Void> {
        val accountLog = AccountLog(
            id = null,
            accountId = event.account.id!!,
            nftId = null,
            accountType = event.accountType,
            timestamp = event.timestamp,
            balance = event.account.balance,
            transferType = transferType
        )
        return accountLogRepository.save(accountLog).then()
    }

    fun saveAccountNft(event: AccountNftEvent, transferType: TransferType) : Mono<Void> {
        val accountLog = AccountLog(
            id = null,
            accountId = event.accountNft.accountId,
            nftId = event.accountNft.nftId,
            accountType = event.accountType,
            timestamp = event.timestamp,
            balance = null,
            transferType = transferType
        )
        return accountLogRepository.save(accountLog).then()
    }
}