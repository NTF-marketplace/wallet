package com.api.wallet.service.api

import com.api.wallet.RedisService
import com.api.wallet.controller.dto.request.DepositRequest
import com.api.wallet.controller.dto.request.WithdrawERC20Request
import com.api.wallet.controller.dto.request.WithdrawERC721Request
import com.api.wallet.controller.dto.response.AccountResponse
import com.api.wallet.controller.dto.response.AccountResponse.Companion.toResponse
import com.api.wallet.controller.dto.response.NftMetadataResponse
import com.api.wallet.domain.account.Account
import com.api.wallet.domain.account.AccountRepository
import com.api.wallet.domain.account.nft.AccountNft
import com.api.wallet.domain.account.nft.AccountNftRepository
import com.api.wallet.domain.nft.repository.NftRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.TransferType
import com.api.wallet.event.AccountEvent
import com.api.wallet.event.AccountNftEvent
import com.api.wallet.rabbitMQ.dto.AdminTransferResponse
import com.api.wallet.service.external.admin.AdminApiService
import com.api.wallet.storage.PriceStorage
import com.api.wallet.util.Util.toPagedMono
import com.api.wallet.util.Util.toTokenType
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.math.BigDecimal

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val walletRepository: WalletRepository,
    @Lazy private val walletService: WalletService,
    private val nftRepository: NftRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val accountNftRepository: AccountNftRepository,
    private val priceStorage: PriceStorage,
    private val redisService: RedisService,
    private val adminApiService: AdminApiService,
    ) {


    // fun findByAccountsByAddress(nftId: Long ,address:String) : Flux<AccountResponse> {
    //     return findAllAccountByAddress(address, chainType)
    //         .flatMap { account ->
    //             accountNftRepository.findByAccountId(account.id!!)
    //         }
    // }

    fun findByAccountsByAddress(address:String) : Flux<AccountResponse> {
        return walletRepository.findAllByAddress(address)
            .flatMap { findByAccountByWallet(it) }
            .collectList()
            .flatMapMany { Flux.fromIterable(it) }
    }


    fun findByAccountByWallet(wallet: Wallet): Mono<AccountResponse> {
        return accountRepository.findByWalletId(wallet.id!!)
            .map { account ->
                val usdt = priceStorage.get(wallet.chainType.toTokenType())
                account.toResponse(usdt,wallet.chainType)
            }
    }

    fun findByAccountNftByAddress(address: String, chainType: ChainType?, pageable: Pageable): Mono<Page<NftMetadataResponse>> {
        return findAllAccountByAddress(address, chainType)
            .flatMap { account ->
                accountNftRepository.findByAccountId(account.id!!)
            }
            .map { it.nftId }
            .collectList()
            .flatMap { nftIds ->
                toPagedMono(redisService.getNfts(nftIds),pageable)
            }
    }

    fun findAllAccountByAddress(address: String, chainType: ChainType?): Flux<Account> {
        return walletService.findWallet(address,chainType)
            .flatMap {
                findByAccountOrCreate(it)
        }.collectList().flatMapMany { Flux.fromIterable(it) }
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
        val updatedBalance = account.updateBalance(transfer.balance!!)
        return accountRepository.save(updatedBalance)
            .doOnSuccess { savedAccount ->
                eventPublisher.publishEvent(AccountEvent(savedAccount, transfer.accountType, transfer.timestamp))
            }
            .then()
    }

    fun depositProcess(address: String , request: DepositRequest): Mono<ResponseEntity<Void>>{
        return adminApiService.createDeposit(address,request)
    }

    fun withdrawERC20Process(address: String, request: WithdrawERC20Request) {


    }

    fun withdrawERC721Process(address: String, request: WithdrawERC721Request) {
        // 해당 nft 네트워크 체크


    }

}