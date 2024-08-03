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
import com.api.wallet.enums.AccountType
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.StatusType
import com.api.wallet.enums.TransferType
import com.api.wallet.event.AccountEvent
import com.api.wallet.event.AccountNftEvent
import com.api.wallet.rabbitMQ.dto.AdminTransferResponse
import com.api.wallet.rabbitMQ.dto.AuctionResponse
import com.api.wallet.rabbitMQ.dto.ListingResponse
import com.api.wallet.service.external.admin.AdminApiService
import com.api.wallet.storage.PriceStorage
import com.api.wallet.util.Util.toPagedMono
import com.api.wallet.util.Util.toTokenType
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.math.BigDecimal
import javax.naming.InsufficientResourcesException

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

    fun checkAccountNftId(address: String, nftId: Long): Mono<Boolean> {
        return accountNftRepository.findByNftIdAndWalletAddressAndChainType(nftId, address)
            .flatMap {
                Mono.just(true)
            }
            .switchIfEmpty(Mono.just(false))
    }

    fun checkAccountBalance(address: String, chainType: ChainType, requiredBalance: BigDecimal): Mono<Boolean> {
        return walletRepository.findByAddressAndChainType(address, chainType)
            .flatMap { wallet ->
                findByAccountOrCreate(wallet)
                    .map { account ->
                        account.balance >= requiredBalance
                    }
            }
    }

    fun findByAccountsByAddress(address: String, chainType: ChainType?): Flux<AccountResponse> {
        return if (chainType != null) {
            walletRepository.findByAddressAndChainType(address, chainType)
                .flatMapMany { wallet ->
                    findByAccountByWallet(wallet).flux()
                }
        } else {
            walletRepository.findAllByAddress(address)
                .flatMap { wallet ->
                    findByAccountByWallet(wallet)
                }
        }
    }


    // 만약 account가 없으면 어떻게 되는건가?
    fun findByAccountByWallet(wallet: Wallet): Mono<AccountResponse> {
        return accountRepository.findByWalletId(wallet.id!!)
            .map { account ->
                val usdt = priceStorage.get(wallet.chainType.toTokenType())
                account.toResponse(usdt,wallet.chainType)
            }
    }

    fun findByAccountNftByAddress(address: String, chainType: ChainType?, pageable: Pageable): Mono<Page<NftMetadataResponse>> {
        return findAccountByAddress(address, chainType)
            .flatMap { account ->
                accountNftRepository.findByAccountId(account.id!!)
            }
            .map { it.nftId }
            .collectList()
            .flatMap { nftIds ->
                toPagedMono(redisService.getNfts(nftIds),pageable)
            }
    }

    fun findAccountByAddress(address: String, chainType: ChainType?): Flux<Account> {
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


    fun saveAccountTransfer(transfer: AdminTransferResponse): Mono<Void> {
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
            processERC721Transfer(
                account,
                transfer.accountType,
                transfer.nftId!!,
                transfer.timestamp
            )
        } else {
            processERC20Transfer(
                account,
                transfer.accountType,
                transfer.balance!!,
                transfer.timestamp
            )
        }
    }

    fun processERC721Transfer(account: Account, accountType: AccountType, nftId:Long, timestamp: Long): Mono<Void> {
        return when (accountType) {
            AccountType.DEPOSIT -> depositERC721(account, nftId,timestamp)
            AccountType.WITHDRAW -> withdrawERC721(account, nftId, timestamp)
        }
    }

    fun depositERC721(account: Account, nftId:Long, timestamp:Long): Mono<Void> {
        return nftRepository.findById(nftId!!)
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
                            AccountType.DEPOSIT,
                            timestamp,
                        ))
                    }
                    .then()
            }
    }

    fun withdrawERC721(account: Account, nftId: Long, timestamp: Long): Mono<Void> {
        return accountNftRepository.findByAccountIdAndNftId(account.id!!, nftId)
            .switchIfEmpty(Mono.error(RuntimeException("NFT with id $nftId not found for account ${account.id}")))
            .flatMap { accountNft ->
                accountNftRepository.delete(accountNft)
                    .doOnSuccess {
                        eventPublisher.publishEvent(AccountNftEvent(
                            accountNft,
                            AccountType.WITHDRAW,
                            timestamp,
                        ))
                    }
                    .then()
            }
    }


    fun processERC20Transfer(account: Account, accountType: AccountType, balance: BigDecimal, timestamp: Long): Mono<Void> {
        return try {
            val updatedAccount = when (accountType) {
                AccountType.DEPOSIT -> account.deposit(balance)
                AccountType.WITHDRAW -> account.withdraw(balance)
            }

            accountRepository.save(updatedAccount)
                .doOnSuccess { savedAccount ->
                    eventPublisher.publishEvent(AccountEvent(savedAccount, accountType, timestamp, balance))
                }
                .then()
        } catch (ex: RuntimeException) {
            Mono.error(ex)
        }
    }


    // 상태처리
    fun depositProcess(address: String , request: DepositRequest): Mono<ResponseEntity<Void>>{
        return adminApiService.createDeposit(address,request)
    }

    // 가스비를 클라이언트가 요청하는건 어떨까?
    // 상태처리
    fun withdrawERC20Process(address: String, request: WithdrawERC20Request): Mono<ResponseEntity<Void>> {
        return walletRepository.findByAddressAndChainType(address, request.chainType)
            .flatMap { wallet ->
                findByAccountOrCreate(wallet)
            }
            .flatMap { account ->
                if (account.balance >= request.amount) {
                    adminApiService.withdrawERC20(address, request)
                } else {
                    Mono.error(InsufficientResourcesException("Insufficient balance"))
                }
            }
            .map { ResponseEntity.ok().build<Void>() }
            .onErrorResume { e ->
                when (e) {
                    is InsufficientResourcesException -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build())
                    else -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                }
            }
    }


    //상태처리
    // accountNft가 LISTING,AUCTION,RESERVATION이 아닌건 바로 에러반환
    fun withdrawERC721Process(address: String, request: WithdrawERC721Request): Mono<ResponseEntity<Void>> {
        return accountNftRepository.findByNftIdAndWalletAddressAndChainType(request.nftId, address)
            .switchIfEmpty(Mono.error(InsufficientResourcesException("NFT not found for account")))
            .flatMap {
                adminApiService.withdrawERC721(address, request)
                    .map { ResponseEntity<Void>(HttpStatus.OK) }
            }
            .onErrorResume { e ->
                when (e) {
                    is InsufficientResourcesException -> Mono.just(ResponseEntity<Void>(HttpStatus.BAD_REQUEST))
                    else -> Mono.just(ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR))
                }
            }
    }


    fun updateListing(newListing: ListingResponse): Mono<Void> {
        return accountNftRepository.findByNftIdAndWalletAddressAndChainType(newListing.nftId, newListing.address)
            .flatMap { accountNft ->
                println("statusType : " + newListing.statusType)
                val updatedStatus = when (newListing.statusType) {
                    StatusType.RESERVATION_CANCEL, StatusType.CANCEL, StatusType.EXPIRED -> StatusType.NONE
                    StatusType.ACTIVED -> StatusType.LISTING
                    else -> newListing.statusType
                }

                val updatedAccountNft = accountNft.update(updatedStatus)
                accountNftRepository.save(updatedAccountNft)
            }.then()
    }

    fun updateAuction(newAuction: AuctionResponse): Mono<Void> {
        return accountNftRepository.findByNftIdAndWalletAddressAndChainType(newAuction.nftId, newAuction.address)
            .flatMap { accountNft ->
                val updatedStatus = when (newAuction.statusType) {
                    StatusType.RESERVATION_CANCEL, StatusType.CANCEL, StatusType.EXPIRED -> StatusType.NONE
                    StatusType.ACTIVED -> StatusType.AUCTION
                    else -> newAuction.statusType
                }

                val updatedAccountNft = accountNft.update(updatedStatus)
                accountNftRepository.save(updatedAccountNft)
            }.then()
    }

}