package com.api.wallet.service.api

import com.api.wallet.controller.dto.response.AccountLogResponse
import com.api.wallet.domain.account.Account
import com.api.wallet.domain.account.AccountRepository
import com.api.wallet.domain.account.log.AccountLog
import com.api.wallet.domain.account.log.AccountLogRepository
import com.api.wallet.domain.nft.repository.NftRepository
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.AccountType
import com.api.wallet.event.AccountCreatedEvent
import com.api.wallet.rabbitMQ.dto.AdminTransferResponse
import com.api.wallet.service.external.nft.NftApiService
import com.api.wallet.service.external.nft.dto.NftResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val walletRepository: WalletRepository,
    private val nftRepository: NftRepository,
    private val nftApiService: NftApiService,
    private val eventPublisher: ApplicationEventPublisher,
    private val accountLogRepository: AccountLogRepository,

) {
    fun readAllAccounts(address: String): Flux<NftResponse> {
        return walletRepository.findAllByAddress(address)
            .next()
            .flatMap {
                accountRepository.findAllByUserId(it.userId).map { it.nftId }
                    .collectList()
                    .filterWhen { Mono.just(it.isNotEmpty())  } }
                    .flatMapMany { ids ->
                        nftApiService.getNfts(ids)
                    }
            .switchIfEmpty(Flux.empty())
    }

    fun readAllAccountLog(
        address: String,
        accountType: AccountType?,
        pageable: Pageable
    ): Flux<AccountLogResponse> {
        return walletRepository.findAllByAddress(address)
            .next()
            .flatMapMany { wallet ->
                getAllAccountLogsBy(wallet.userId,accountType,pageable)
            }
            .collectList()
            .filterWhen {  Mono.just(it.isNotEmpty()) }
            .flatMapMany { accountLogs->
                val nftIds = accountLogs.map { it.nftId }
                nftApiService.getNfts(nftIds)
                    .collectList()
                    .flatMapMany {nftResponses ->
                        Flux.fromIterable(mapToAccountLogResponses(accountLogs,nftResponses))
                    }
            }
            .switchIfEmpty(Flux.empty())
    }

    private fun mapToAccountLogResponses(accountLogs: List<AccountLog>, nftResponses: List<NftResponse>): List<AccountLogResponse> {
        val nftMap = nftResponses.associateBy { it.id }
        return accountLogs.map { accountLog ->
            val nftResponse = nftMap[accountLog.nftId]!!
            AccountLogResponse(
                nftResponse = nftResponse,
                timestamp = accountLog.timestamp,
                accountType = accountLog.accountType
            )
        }
    }


    private fun getAllAccountLogsBy(userId: Long, accountType: AccountType?, pageable: Pageable) : Flux<AccountLog> {
        val sortedPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.DESC, "timestamp"))
        return if (accountType != null) {
            accountLogRepository.findAllByUserIdAndAccountType(userId, accountType.toString(), sortedPageable)
        }else{
            accountLogRepository.findAllByUserId(userId, sortedPageable)
        }
    }
    fun saveAccountNfts(transfer: AdminTransferResponse): Mono<Void> {
        return walletRepository.findAllByAddress(transfer.walletAddress)
            .next()
            .flatMap { wallet ->
                nftRepository.findById(transfer.nftId)
                    .flatMap { nft ->
                        val account = Account(
                            id = null,
                            userId = wallet.userId,
                            nftId = nft.id,
                        )
                        accountRepository.save(account)
                            .doOnSuccess { eventPublisher.publishEvent(AccountCreatedEvent(it,transfer.accountType,transfer.timestamp)) }
                            .then()
                    }
            }
            .onErrorResume { error ->
                println("Error: ${error.message}")
                Mono.empty()
            }
    }



}