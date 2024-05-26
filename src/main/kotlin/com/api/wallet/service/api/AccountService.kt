package com.api.wallet.service.api

import com.api.wallet.domain.account.Account
import com.api.wallet.domain.account.AccountRepository
import com.api.wallet.domain.account.nft.AccountNft
import com.api.wallet.domain.account.nft.AccountNftRepository
import com.api.wallet.domain.nft.repository.NftRepository
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.TransferType
import com.api.wallet.event.AccountEvent
import com.api.wallet.event.AccountNftEvent
import com.api.wallet.rabbitMQ.dto.AdminTransferResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.math.BigDecimal

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val walletRepository: WalletRepository,
    private val nftRepository: NftRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val accountNftRepository: AccountNftRepository,

) {
    fun findByAccountOrCreate(userId: Long) : Mono<Account> {
        return accountRepository.findByUserId(userId).switchIfEmpty {
            accountRepository.save(
                Account(
                    id = null,
                    userId = userId,
                    balance = BigDecimal.ZERO
                )
            )
        }
    }

    fun saveAccount(transfer: AdminTransferResponse): Mono<Void> {
        return walletRepository.findAllByAddress(transfer.walletAddress)
            .next()
            .flatMap { wallet ->
                findByAccountOrCreate(wallet.userId)
            }
            .flatMap { processTransfer(it,transfer) }
            .then()
            .onErrorResume { error ->
                println("Error: ${error.message}")
                Mono.empty()
            }
    }


    private fun processTransfer(account: Account, transfer: AdminTransferResponse): Mono<Void> {
        return if (transfer.transferType == TransferType.ERC721) {
            processERC721Transfer(account, transfer)
        } else {
            processERC20Transfer(account, transfer)
        }
    }

    private fun processERC721Transfer(account: Account, transfer: AdminTransferResponse): Mono<Void> {
        return nftRepository.findById(transfer.nftId!!)
            .flatMap { nft ->
                val accountNft = AccountNft(
                    id = null,
                    accountId = account.id!!,
                    nftId = nft.id
                )
                accountNftRepository.save(accountNft)
                    .doOnSuccess { savedAccountNft ->
                        eventPublisher.publishEvent(AccountNftEvent(
                            savedAccountNft,
                            transfer.accountType,
                            transfer.timestamp,
                        ))
                    }
                    .then()
            }
    }

    private fun processERC20Transfer(account: Account, transfer: AdminTransferResponse): Mono<Void> {
        account.updateBalance(transfer.balance!!)
        return accountRepository.save(account)
            .doOnSuccess { savedAccount ->
                eventPublisher.publishEvent(AccountEvent(savedAccount, transfer.accountType, transfer.timestamp))
            }
            .then()
    }
}