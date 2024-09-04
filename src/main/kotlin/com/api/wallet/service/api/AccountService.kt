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
import com.api.wallet.domain.account.detail.AccountDetailLog
import com.api.wallet.domain.account.log.AccountLogRepository
import com.api.wallet.domain.account.nft.AccountNft
import com.api.wallet.domain.account.nft.AccountNftRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.*
import com.api.wallet.kafka.dto.SaleResponse
import com.api.wallet.rabbitMQ.dto.AdminTransferResponse
import com.api.wallet.service.external.admin.AdminApiService
import com.api.wallet.storage.PriceStorage
import com.api.wallet.util.Util.toPagedMono
import com.api.wallet.util.Util.toTokenType
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
    private val accountNftRepository: AccountNftRepository,
    private val priceStorage: PriceStorage,
    private val redisService: RedisService,
    private val adminApiService: AdminApiService,
    @Lazy private val accountLogService: AccountLogService,
    private val accountLogRepository: AccountLogRepository,
    private val accountDetailLogService: AccountDetailLogService,
    ) {

    fun checkAccountNftId(address: String, nftId: Long): Mono<Boolean> {
        return redisService.getNft(nftId).flatMap {
            accountNftRepository.findByNftIdAndWalletAddressAndChainType(it.id, address,it.chainType)
                .hasElement()
        }
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

    fun findByAccountByWallet(wallet: Wallet): Mono<AccountResponse> {
        return findByAccountOrCreate(wallet)
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

    @Transactional
    fun processTransfer(transfer: AdminTransferResponse): Mono<Void> {
        return accountLogRepository.findById(transfer.accountLogId)
            .flatMap { accountLog ->
                val updatedLog = accountLog.update(transactionStatusType = transfer.transactionStatusType, null)
                accountLogRepository.save(updatedLog)
            }
            .flatMap { updatedLog ->
                if (transfer.transactionStatusType == TransaionStatusType.SUCCESS) {
                    accountRepository.findById(updatedLog.accountId)
                        .flatMap { account ->
                            when (transfer.transferType) {
                                TransferType.ERC721 -> processERC721Transfer(account, transfer.accountType, transfer.adminTransferDetailResponse?.nftId!!)
                                TransferType.ERC20 -> processERC20Transfer(account, transfer.accountType, transfer.adminTransferDetailResponse?.balance!!)
                            }
                        }
                        .flatMap { accountDetailLog ->
                            val logWithDetailId = updatedLog.copy(accountDetailLogId = accountDetailLog.id)
                            accountLogRepository.save(logWithDetailId)
                                .then()
                        }
                } else {
                    Mono.just(updatedLog).then()
                }
            }
    }


    fun processERC721Transfer(account: Account, accountType: AccountType,nftId: Long): Mono<AccountDetailLog> {
        return when (accountType) {
            AccountType.DEPOSIT -> depositERC721(account, nftId)
            AccountType.WITHDRAW -> withdrawERC721(account, nftId)
        }
    }

    fun depositERC721(account: Account,nftId: Long): Mono<AccountDetailLog> {
        return redisService.getNft(nftId)
            .flatMap { nft ->
                val accountNft = AccountNft(
                    id = null,
                    accountId = account.id!!,
                    nftId = nft.id
                )
                accountNftRepository.save(accountNft)
                    .flatMap { savedAccountNft ->
                        accountDetailLogService.saveAccountNft(
                              savedAccountNft,
                              TransferType.ERC721
                        )
                    }
            }
    }

    fun withdrawERC721(account: Account, nftId: Long): Mono<AccountDetailLog> {
        return accountNftRepository.findByAccountIdAndNftId(account.id!!, nftId)
            .switchIfEmpty(Mono.error(RuntimeException("NFT with id $nftId not found for account ${account.id}")))
            .flatMap { accountNft ->
                accountNftRepository.delete(accountNft)
                    .then(
                        accountDetailLogService.saveAccountNft(accountNft, TransferType.ERC721)
                    )
            }
    }



    fun processERC20Transfer(account: Account, accountType: AccountType, balance: BigDecimal): Mono<AccountDetailLog> {
        return try {
            val updatedAccount = when (accountType) {
                AccountType.DEPOSIT -> account.deposit(balance)
                AccountType.WITHDRAW -> account.withdraw(balance)
            }

            accountRepository.save(updatedAccount)
                .flatMap {
                    accountDetailLogService.saveAccountLog(TransferType.ERC20,balance)
                }
        } catch (ex: RuntimeException) {
            Mono.error(ex)
        }
    }


    fun depositProcess(address: String, request: DepositRequest): Mono<Void> {
        return findAccountByAddress(address, request.chainType)
            .next()
            .flatMap { account ->
                accountLogService.save(account.id!!, AccountType.DEPOSIT)
                    .map { savedAccountLogId ->
                        request.copy(accountLogId = savedAccountLogId.id)
                    }
            }
            .flatMap { updatedRequest ->
                adminApiService.createDeposit(address, updatedRequest)
            }.then()
    }

    fun withdrawERC20Process(address: String, request: WithdrawERC20Request): Mono<Void> {
        return findAccountByAddress(address,request.chainType)
            .next()
            .flatMap { account ->
                if (account.balance >= request.amount) {
                    accountLogService.save(account.id!!, AccountType.WITHDRAW)
                        .flatMap { accountLog ->
                            val updatedRequest = request.copy(accountLogId = accountLog.id!!)
                            adminApiService.withdrawERC20(address, updatedRequest)
                        }
                } else {
                    Mono.error(InsufficientResourcesException("Insufficient balance"))
                }
            }.then()
    }


    fun withdrawERC721Process(address: String, request: WithdrawERC721Request): Mono<Void> {
        return redisService.getNft(request.nftId).flatMap {
            accountNftRepository.findByNftIdAndWalletAddressAndChainType(it.id, address,it.chainType)
                .switchIfEmpty(Mono.error(InsufficientResourcesException("NFT not found for account")))
                .flatMap { accountNft ->
                    accountLogService.save(accountNft.accountId, AccountType.WITHDRAW)
                        .flatMap { accountLog ->
                            val updatedRequest = request.copy(accountLogId = accountLog.id!!)
                            adminApiService.withdrawERC721(address, updatedRequest)
                                .map { ResponseEntity<Void>(HttpStatus.OK) }
                        }
                }
        }.then()
    }


    fun updateListing(newListing: SaleResponse): Mono<Void> {
        return redisService.getNft(newListing.nftId).flatMap {
            accountNftRepository.findByNftIdAndWalletAddressAndChainType(it.id, newListing.address, it.chainType)
                .flatMap { accountNft ->
                    val updatedStatus = when (newListing.statusType) {
                        StatusType.RESERVATION_CANCEL, StatusType.CANCEL, StatusType.EXPIRED, StatusType.LEDGER -> StatusType.NONE
                        StatusType.ACTIVED -> StatusType.LISTING
                        else -> newListing.statusType
                    }

                    val updatedAccountNft = accountNft.update(updatedStatus)
                    accountNftRepository.save(updatedAccountNft)
                }.then()
        }
    }

        fun updateAuction(newAuction: SaleResponse): Mono<Void> {
            return redisService.getNft(newAuction.nftId).flatMap {
                accountNftRepository.findByNftIdAndWalletAddressAndChainType(it.id, newAuction.address, it.chainType)
                    .flatMap { accountNft ->
                        val updatedStatus = when (newAuction.statusType) {
                            StatusType.RESERVATION_CANCEL, StatusType.CANCEL, StatusType.EXPIRED -> StatusType.NONE
                            StatusType.ACTIVED -> StatusType.AUCTION
                            else -> newAuction.statusType
                        }

                        val updatedAccountNft = accountNft.update(updatedStatus)
                        accountNftRepository.save(updatedAccountNft)
                    }
            }.then()
        }

    }