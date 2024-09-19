package com.api.wallet.service.api

import com.api.wallet.RedisService
import com.api.wallet.controller.dto.response.AccountLogDetailResponse
import com.api.wallet.controller.dto.response.AccountLogResponse
import com.api.wallet.controller.dto.response.NftMetadataResponse
import com.api.wallet.domain.account.detail.AccountDetailLog
import com.api.wallet.domain.account.detail.AccountDetailLogRepository
import com.api.wallet.domain.account.log.AccountLog
import com.api.wallet.domain.account.log.AccountLogRepository
import com.api.wallet.enums.AccountType
import com.api.wallet.enums.TransferType
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
    private val accountDetailLogRepository: AccountDetailLogRepository
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
            accountLogRepository.findByAccountIdIn(accountIds, pageable)
        } else {
            accountLogRepository.findByAccountIdInAndAccountType(accountIds, accountType, pageable)
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
        val accountDetailLogIds = accountLogs.mapNotNull { it.accountDetailLogId }

        return accountDetailLogRepository.findAllById(accountDetailLogIds)
            .collectList()
            .flatMap { accountDetailLogs ->
                val accountDetailLogMap = accountDetailLogs.associateBy { it.id }
                val nftIds = accountDetailLogs.mapNotNull { it.nftId }

                redisService.getNfts(nftIds)
                    .collectList()
                    .flatMap { nfts ->
                        val nftMap = nfts.associateBy { it.id }

                        val accountLogResponses = accountLogs.map { accountLog ->
                            val accountDetailLog = accountLog.accountDetailLogId?.let { accountDetailLogMap[it] }
                            val nftResponse = accountDetailLog?.nftId?.let { nftMap[it] }

                            accountLog.toResponse(nftResponse, accountDetailLog)
                        }

                        Mono.just(accountLogResponses)
                    }
            }
    }

    fun AccountLog.toResponse(nftResponse: NftMetadataResponse?, accountDetailLog: AccountDetailLog?): AccountLogResponse {
        val detail = accountDetailLog?.let {
            AccountLogDetailResponse(
                nftResponse = if (it.transferType == TransferType.ERC721) nftResponse else null,
                balance = it.balance ?: BigDecimal.ZERO,
                transferType = it.transferType
            )
        }

        return AccountLogResponse(
            timestamp = this.createdAt ?: 0L,
            accountType = this.accountType.name,
            transactionStatusType = this.transactionStatusType,
            detail = detail
        )
    }
    fun save(accountId: Long, accountType: AccountType): Mono<AccountLog> {
        return accountLogRepository.save(AccountLog(
            accountId = accountId,
            accountType = accountType,
        ))
    }

}