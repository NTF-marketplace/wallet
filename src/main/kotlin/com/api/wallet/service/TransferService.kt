package com.api.wallet.service

import com.api.wallet.controller.dto.request.TransferRequest
import com.api.wallet.domain.account.Account
import com.api.wallet.enums.AccountType
import com.api.wallet.service.api.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class TransferService(
    private val accountService: AccountService,
) {

    @Transactional
    fun transfer(request: TransferRequest): Mono<ResponseEntity<String>> {
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
            .flatMap { fromAccounts ->
                val fromAccount = fromAccounts.firstOrNull()
                    ?: return@flatMap Mono.error(IllegalArgumentException("From account not found"))

                accountService.findAccountByAddress(request.toAddress, request.chainType)
                    .collectList()
                    .flatMap { toAccounts ->
                        val toAccount = toAccounts.firstOrNull()
                            ?: return@flatMap Mono.error(IllegalArgumentException("To account not found"))
                        Mono.just(fromAccount to toAccount)
                    }
            }
    }

    private fun updateBalances(fromAccount: Account, toAccount: Account, amount: BigDecimal): Mono<Void> {
        val timestamp = System.currentTimeMillis()

        return accountService.processERC20Transfer(fromAccount, AccountType.WITHDRAW, balance = amount, timestamp = timestamp)
            .then(accountService.processERC20Transfer(toAccount, AccountType.DEPOSIT, balance = amount, timestamp = timestamp))
            .onErrorResume { error ->
                Mono.error(IllegalArgumentException("Failed to update balances: ${error.message}"))
            }
    }


    fun transferNft(fromAccount: Account, toAccount: Account, nftId: Long): Mono<Void> {
        val timestamp = System.currentTimeMillis()

        return accountService.processERC721Transfer(toAccount, AccountType.WITHDRAW, nftId, timestamp)
            .then(accountService.processERC721Transfer(fromAccount, AccountType.DEPOSIT, nftId, timestamp))
            .onErrorResume { error ->
                Mono.error(IllegalArgumentException("Failed to update transferNft: ${error.message}"))
            }
    }


}