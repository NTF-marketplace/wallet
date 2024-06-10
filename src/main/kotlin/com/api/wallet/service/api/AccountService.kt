package com.api.wallet.service.api

import com.api.wallet.controller.dto.response.AccountResponse
import com.api.wallet.controller.dto.response.AccountResponse.Companion.toResponse
import com.api.wallet.domain.account.Account
import com.api.wallet.domain.account.AccountRepository
import com.api.wallet.domain.account.nft.AccountNft
import com.api.wallet.domain.account.nft.AccountNftRepository
import com.api.wallet.domain.nft.Nft
import com.api.wallet.domain.nft.repository.NftRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.TransferType
import com.api.wallet.event.AccountEvent
import com.api.wallet.event.AccountNftEvent
import com.api.wallet.rabbitMQ.dto.AdminTransferResponse
import com.api.wallet.service.external.nft.dto.NftResponse
import com.api.wallet.storage.PriceStorage
import com.api.wallet.util.Util.toTokenType
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
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
    private val priceStorage: PriceStorage,

    ) {
    fun findByAccountByAddress(address: String, chainType: ChainType?): Flux<AccountResponse> {
        return if (chainType != null) {
            walletRepository.findByAddressAndChainType(address, chainType)
                .flatMapMany { wallet ->
                    findByAccountOrCreate(wallet)
                        .map { account ->
                            val usdt = priceStorage.get(wallet.chainType.toTokenType())
                            account.toResponse(wallet.chainType,usdt) }
                }
        } else {
            walletRepository.findAllByAddress(address)
                .flatMap { wallet ->
                    findByAccountOrCreate(wallet)
                        .map { account ->
                            val usdt = priceStorage.get(wallet.chainType.toTokenType())
                            account.toResponse(wallet.chainType,usdt) }
                }
        }
    }

    fun findByAccountNftByAddress(address: String,chainType: ChainType?): Flux<NftResponse> {
        return findAllAccountByAddress(address,chainType)
            .flatMap { account ->
                accountNftRepository.findByAccountId(account.id!!)
                    .flatMap { accountNft ->
                        nftRepository.findById(accountNft.nftId)
                            .map { nft -> accountNft.toNftResponse(nft) }
                    }
            }
    }

    fun findAllAccountByAddress(address: String, chainType: ChainType?): Flux<Account> {
        return if (chainType != null) {
            walletRepository.findByAddressAndChainType(address, chainType)
                .flatMapMany { wallet ->
                    findByAccountOrCreate(wallet).flux()
                }
        } else {
            walletRepository.findAllByAddress(address)
                .flatMap { wallet ->
                    findByAccountOrCreate(wallet).flux()
                }
        }
    }


    private fun AccountNft.toNftResponse(nft: Nft): NftResponse {
        return NftResponse(
            id = this.id!!,
            tokenId = nft.tokenId,
            tokenAddress = nft.tokenAddress,
            chainType = nft.chainType
        )
    }


    fun findByAccountOrCreate(wallet: Wallet) : Mono<Account> {
        return accountRepository.findByWalletId(wallet.id!!).switchIfEmpty {
            accountRepository.save(
                Account(
                    id = null,
                    walletId = wallet.id,
                    balance = BigDecimal.ZERO
                )
            )
        }
    }


    fun saveAccount(transfer: AdminTransferResponse): Mono<Void> {
        return walletRepository.findByAddressAndChainType(transfer.walletAddress,transfer.chainType)
            .flatMap { wallet ->
                findByAccountOrCreate(wallet)
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