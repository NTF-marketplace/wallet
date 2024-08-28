package com.api.wallet.service.api

import com.api.wallet.RedisService
import com.api.wallet.controller.dto.response.AccountLogResponse
import com.api.wallet.controller.dto.response.NftMetadataResponse
import com.api.wallet.domain.account.log.AccountLog
import com.api.wallet.domain.account.log.AccountLogRepository
import com.api.wallet.enums.AccountType
import com.api.wallet.enums.TransferType
import com.api.wallet.event.AccountEvent
import com.api.wallet.event.AccountNftEvent
import com.api.wallet.util.Util.toPage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class AccountLogService(
    private val accountLogRepository: AccountLogRepository,
    private val accountService: AccountService,
    private val redisService: RedisService,
) {
    fun findAllByAccountLog(address: String, accountType: AccountType?, pageable: Pageable): Mono<Page<AccountLogResponse>> {
        return accountService.findAccountByAddress(address, null)
            .mapNotNull { it.id!! }
            .collectList()
            .flatMap { accountIds ->
                val accountLogs = getAccountLogs(accountIds, accountType, pageable)
                val count = getCount(accountIds, accountType)

                accountLogs
                    .collectList()
                    .flatMap { accountLogs ->
                        toAccountLogResponses(accountLogs)
                    }
                    .flatMap { accountLogResponses ->
                        toPage(Flux.fromIterable(accountLogResponses), pageable, count)
                    }
            }
    }

    private fun getAccountLogs(accountIds: List<Long>, accountType: AccountType?, pageable: Pageable): Flux<AccountLog> {
        return if (accountType == null) {
            accountLogRepository.findByAccountIdInOrderByTimestampDesc(accountIds, pageable)
        } else {
            accountLogRepository.findByAccountIdInAndAccountTypeOrderByTimestampDesc(accountIds, accountType, pageable)
        }
    }

    private fun getCount(accountIds: List<Long>, accountType: AccountType?): Mono<Long> {
        return if (accountType == null) {
            accountLogRepository.countByAccountIdIn(accountIds)
        } else {
            accountLogRepository.countByAccountIdInAndAccountType(accountIds, accountType)
        }
    }

    private fun toAccountLogResponses(accountLogs: List<AccountLog>): Mono<List<AccountLogResponse>> {
        val nftIds = accountLogs.filter { it.transferType == TransferType.ERC721 && it.nftId != null }
            .mapNotNull { it.nftId }

        return redisService.getNfts(nftIds)
            .collectList()
            .map { nfts ->
                val nftMap = nfts.associateBy { it.id }
                accountLogs.map { accountLog ->
                    val nftResponse = accountLog.nftId?.let { nftMap[it]?.let { it } }
                    accountLog.toResponse(nftResponse)
                }
            }
    }

    fun AccountLog.toResponse(nftResponse: NftMetadataResponse?): AccountLogResponse {
        return AccountLogResponse(
            nftResponse = if (this.transferType == TransferType.ERC20) null else nftResponse,
            timestamp = this.timestamp,
            accountType = this.accountType.name,
            balance = if (this.transferType == TransferType.ERC721) BigDecimal.ZERO else this.balance ?: BigDecimal.ZERO
        )
    }
    fun saveAccountLog(event: AccountEvent,transferType: TransferType,balance: BigDecimal) : Mono<Void> {
        val accountLog = AccountLog(
            id = null,
            accountId = event.account.id!!,
            nftId = null,
            accountType = event.accountType,
            timestamp = event.timestamp,
            balance = balance,
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