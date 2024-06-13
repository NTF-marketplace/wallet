package com.api.wallet.service.api

import com.api.wallet.controller.dto.response.AccountLogResponse
import com.api.wallet.domain.account.log.AccountLog
import com.api.wallet.domain.account.log.AccountLogRepository
import com.api.wallet.domain.nft.repository.NftRepository
import com.api.wallet.enums.AccountType
import com.api.wallet.enums.TransferType
import com.api.wallet.event.AccountEvent
import com.api.wallet.event.AccountNftEvent
import com.api.wallet.service.external.nft.dto.NftResponse
import com.api.wallet.util.Util.toNftResponse
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
    private val nftRepository: NftRepository,
) {

    fun findAllByAccountLog(address: String, accountType: AccountType, pageable: Pageable): Mono<Page<AccountLogResponse>> {
        return accountService.findAllAccountByAddress(address, null)
            .mapNotNull { it.id!! }
            .collectList()
            .flatMap { accountIds ->
                val accountLogsFlux = accountLogRepository.findByAccountIdInAndAccountTypeOrderByTimestampDesc(accountIds, accountType, pageable)
                    .concatMap { accountLog -> // flatMap 결과 정렬이 흐트러짐 concatMap 으로 순서유지
                        toAccountLogResponse(accountLog)
                    }
                    .collectList().flatMapMany { Flux.fromIterable(it) }

                val countMono = accountLogRepository.countByAccountIdInAndAccountType(accountIds, accountType)

                toPage(accountLogsFlux, pageable, countMono)
            }
    }


    fun toAccountLogResponse(accountLog: AccountLog): Mono<AccountLogResponse> {
        return if (accountLog.transferType == TransferType.ERC721 && accountLog.nftId != null) {
            nftRepository.findById(accountLog.nftId)
                .map { nft -> accountLog.toResponse(toNftResponse(nft) )}
                .defaultIfEmpty(accountLog.toResponse(null))
        } else {
            Mono.just(accountLog.toResponse(null))
        }
    }


    fun AccountLog.toResponse(nftResponse: NftResponse?): AccountLogResponse {
        return AccountLogResponse(
            nftResponse = if (this.transferType == TransferType.ERC20) null else nftResponse,
            timestamp = this.timestamp,
            accountType = this.accountType.name,
            balance = if (this.transferType == TransferType.ERC721) BigDecimal.ZERO else this.balance ?: BigDecimal.ZERO
        )
    }
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