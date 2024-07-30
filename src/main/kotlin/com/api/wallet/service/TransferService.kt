package com.api.wallet.service

import com.api.wallet.controller.dto.request.TransferRequest
import com.api.wallet.domain.account.Account
import com.api.wallet.domain.account.AccountRepository
import com.api.wallet.service.api.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class TransferService(
    private val accountService: AccountService,
    private val accountRepository: AccountRepository,
) {

    @Transactional
    fun transfer(
        request: TransferRequest
    ): Mono<ResponseEntity<String>> {
        return accountService.findAccountByAddress(request.fromAddress, request.chainType)
            .collectList()
            .flatMap { fromAccounts ->
                if (fromAccounts.isEmpty()) {
                    return@flatMap Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("From account not found"))
                }
                val fromAccount = fromAccounts[0]

                if (fromAccount.balance < request.amount) {
                    return@flatMap Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient balance"))
                }

                accountService.findAccountByAddress(request.toAddress, request.chainType)
                    .collectList()
                    .flatMap { toAccounts ->
                        if (toAccounts.isEmpty()) {
                            return@flatMap Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("To account not found"))
                        }
                        val toAccount = toAccounts[0]

                        fromAccount.balance -= request.amount
                        toAccount.balance += request.amount

                        updateAccount(fromAccount)
                            .then(updateAccount(toAccount))
                            .then(Mono.just(ResponseEntity.ok("Transfer successful")))
                            .onErrorResume { ex ->
                                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Transfer failed: ${ex.message}"))
                            }
                    }
            }
    }

    fun updateAccount(account: Account): Mono<Account> {
        return accountRepository.save(account)
    }

}