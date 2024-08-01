package com.api.wallet.service

import com.api.wallet.controller.dto.request.TransferRequest
import com.api.wallet.domain.account.Account
import com.api.wallet.domain.account.AccountRepository
import com.api.wallet.domain.account.nft.AccountNft
import com.api.wallet.domain.account.nft.AccountNftRepository
import com.api.wallet.enums.StatusType
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
    private val accountRepository: AccountRepository,
    private val accountNftRepository: AccountNftRepository,
) {

    // 격리수준을 설정해야되지않을까?
    @Transactional
    fun transfer(request: TransferRequest): Mono<ResponseEntity<String>> {
        return findAndValidateAccounts(request)
            .flatMap { (fromAccount, toAccount) ->
                updateBalances(fromAccount, toAccount, request.amount)
                    .then(transferNft(fromAccount.id!!, toAccount.id!!, request.nftId))
                    .then(Mono.just(ResponseEntity.ok("Transfer and NFT ownership transfer successful")))
                    .onErrorResume { ex ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Transfer failed: ${ex.message}"))
                    }
            }
    }

    private fun findAndValidateAccounts(request: TransferRequest): Mono<Pair<Account, Account>> {
        return accountService.findAccountByAddress(request.fromAddress, request.chainType)
            .collectList()
            .flatMap { fromAccounts ->
                val fromAccount = fromAccounts.firstOrNull()
                    ?: return@flatMap Mono.error(IllegalArgumentException("From account not found"))

                if (fromAccount.balance < request.amount) {
                    return@flatMap Mono.error(IllegalArgumentException("Insufficient balance"))
                }

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
        fromAccount.balance -= amount
        toAccount.balance += amount
        return updateAccount(fromAccount).then()
    }

    private fun transferNft(fromAccountId: Long, toAccountId: Long, nftId: Long): Mono<Void> {
        return accountNftRepository.findByAccountIdAndNftId(toAccountId, nftId)
            .switchIfEmpty(Mono.error(IllegalArgumentException("NFT not found in the target account")))
            .flatMap { nft ->
                accountNftRepository.delete(nft)
                    .then(
                        accountNftRepository.save(
                            AccountNft(
                                accountId = fromAccountId,
                                nftId = nftId,
                                status = StatusType.LEDGER
                            )
                        )
                    )
            }.then()
    }


    fun updateAccount(account: Account): Mono<Account> {
        return accountRepository.save(account)
    }

}