package com.api.wallet.service.api

import com.api.wallet.domain.account.detail.AccountDetailLog
import com.api.wallet.domain.account.detail.AccountDetailLogRepository
import com.api.wallet.domain.account.nft.AccountNft
import com.api.wallet.enums.TransferType
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class AccountDetailLogService(
    private val accountDetailLogRepository: AccountDetailLogRepository,
) {

    fun saveAccountLog(transferType: TransferType, balance: BigDecimal) : Mono<AccountDetailLog> {
        val accountDetail = AccountDetailLog(
            id = null,
            nftId = null,
            balance = balance,
            transferType = transferType
        )
        return accountDetailLogRepository.save(accountDetail)
    }

    fun saveAccountNft(accountNft: AccountNft, transferType: TransferType) : Mono<AccountDetailLog> {
        val accountDetail = AccountDetailLog(
            id = null,
            nftId = accountNft.nftId,
            balance = null,
            transferType = transferType
        )
        return accountDetailLogRepository.save(accountDetail)
    }


}