package com.api.wallet.service.api

import com.api.wallet.controller.dto.request.TransferRequest
import com.api.wallet.domain.account.Account
import com.api.wallet.domain.account.detail.AccountDetailLog
import com.api.wallet.domain.account.log.AccountLogRepository
import com.api.wallet.enums.AccountType
import com.api.wallet.enums.TransaionStatusType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class TransferService(
    private val accountService: AccountService,
    private val accountLogService: AccountLogService,
    private val accountLogRepository: AccountLogRepository,
) {

    @Transactional
    fun transfer(request: TransferRequest): Mono<ResponseEntity<String>> {
        println("request : " + request.toString())
        return findAndValidateAccounts(request)
            .flatMap { (fromAccount, toAccount) ->
                updateBalances(fromAccount, toAccount, request.amount)
                    .then(transferNft(fromAccount, toAccount, request.nftId))
                    .then(Mono.just(ResponseEntity.ok("Transfer and NFT ownership transfer successful")))
            }
            .onErrorResume { ex ->
                when (ex) {
                    is IllegalArgumentException -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Transfer failed: ${ex.message}"))
                    else -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Transfer failed: ${ex.message}"))
                }
            }
    }

    private fun findAndValidateAccounts(request: TransferRequest): Mono<Pair<Account, Account>> {
        return accountService.findAccountByAddress(request.fromAddress, request.chainType)
            .collectList()
            .doOnNext { fromAccounts ->
                println("fromAccounts: $fromAccounts")
            }
            .flatMap { fromAccounts ->
                val fromAccount = fromAccounts.firstOrNull()
                    ?: return@flatMap Mono.error(IllegalArgumentException("From account not found"))

                accountService.findAccountByAddress(request.toAddress, request.chainType)
                    .collectList()
                    .doOnNext { toAccounts ->
                        println("toAccounts: $toAccounts")
                    }
                    .flatMap { toAccounts ->
                        val toAccount = toAccounts.firstOrNull()
                            ?: return@flatMap Mono.error(IllegalArgumentException("To account not found"))
                        Mono.just(fromAccount to toAccount)
                    }
            }
    }

    private fun updateBalances(fromAccount: Account, toAccount: Account, amount: BigDecimal): Mono<Void> {
        return accountLogService.save(fromAccount.id!!, AccountType.WITHDRAW)
            .flatMap { fromAccountLog ->
                accountLogService.save(toAccount.id!!, AccountType.DEPOSIT)
                    .doOnSuccess { toAccountLog ->
                        println("Saved toAccountLog: $toAccountLog")
                    }
                    .flatMap { toAccountLog ->
                        Mono.zip(
                            accountService.processERC20Transfer(fromAccount, AccountType.WITHDRAW, balance = amount),
                            accountService.processERC20Transfer(toAccount, AccountType.DEPOSIT, balance = amount)
                        ).flatMap { tuple ->
                            val fromAccountDetailLog = tuple.t1
                            val toAccountDetailLog = tuple.t2

                            updateAccountLogs(
                                fromAccountLogId = fromAccountLog.id!!,
                                fromAccountDetailLog = fromAccountDetailLog,
                                toAccountLogId = toAccountLog.id!!,
                                toAccountDetailLog = toAccountDetailLog,
                                transactionStatusType = TransaionStatusType.SUCCESS
                            )
                        }
                    }
            }
            .onErrorResume { error ->
                println("Error during balance update: ${error.message}")
                updateAccountLogs(
                    fromAccountLogId = fromAccount.id!!,
                    fromAccountDetailLog = null,
                    toAccountLogId = toAccount.id!!,
                    toAccountDetailLog = null,
                    transactionStatusType = TransaionStatusType.FAIL
                ).then(Mono.error(IllegalArgumentException("Failed to update balances: ${error.message}")))
            }
    }


    private fun updateAccountLogs(fromAccountLogId: Long,
                                  fromAccountDetailLog: AccountDetailLog?,
                                  toAccountLogId: Long,
                                  toAccountDetailLog: AccountDetailLog?,
                                  transactionStatusType: TransaionStatusType
    ): Mono<Void> {
        return accountLogRepository.findById(fromAccountLogId)
            .flatMap { fromAccountLog ->
                val updatedFromLog = fromAccountLog.update(accountDetailLog = fromAccountDetailLog, transactionStatusType = transactionStatusType)
                accountLogRepository.save(updatedFromLog)
            }
            .then(accountLogRepository.findById(toAccountLogId))
            .flatMap { toAccountLog ->
                val updatedToLog = toAccountLog.update(accountDetailLog = toAccountDetailLog, transactionStatusType = transactionStatusType)
                accountLogRepository.save(updatedToLog)
            }
            .then()
    }

    fun transferNft(fromAccount: Account, toAccount: Account, nftId: Long): Mono<Void> {
        return accountLogService.save(fromAccount.id!!, AccountType.WITHDRAW)
            .flatMap { fromAccountLog ->
                accountLogService.save(toAccount.id!!, AccountType.DEPOSIT)
                    .flatMap { toAccountLog ->
                        Mono.zip(
                            accountService.processERC721Transfer(toAccount, AccountType.WITHDRAW, nftId),
                            accountService.processERC721Transfer(fromAccount, AccountType.DEPOSIT, nftId)
                        ).flatMap { tuple ->
                            val fromAccountDetailLog = tuple.t1
                            val toAccountDetailLog = tuple.t2
                            updateAccountLogs(
                                fromAccountLogId = fromAccountLog.id!!,
                                fromAccountDetailLog = fromAccountDetailLog,
                                toAccountLogId = toAccountLog.id!!,
                                toAccountDetailLog = toAccountDetailLog,
                                transactionStatusType = TransaionStatusType.SUCCESS
                            )
                        }
                    }
            }
            .onErrorResume { error ->
                println("Error during NFT transfer: ${error.message}")
                updateAccountLogs(
                    fromAccountLogId = fromAccount.id,
                    fromAccountDetailLog = null,
                    toAccountLogId = toAccount.id!!,
                    toAccountDetailLog = null,
                    transactionStatusType = TransaionStatusType.FAIL
                ).then(Mono.error(IllegalArgumentException("Failed to transfer NFT: ${error.message}")))
            }
    }
}