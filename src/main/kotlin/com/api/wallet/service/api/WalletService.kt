package com.api.wallet.service.api

import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.controller.dto.response.SignInResponse
import com.api.wallet.controller.dto.response.WalletAccountResponse
import com.api.wallet.domain.user.Users
import com.api.wallet.domain.user.repository.UserRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.ChainType
import com.api.wallet.service.external.auth.AuthApiService
import com.api.wallet.service.external.infura.InfuraApiService
import com.api.wallet.validator.SignatureValidator
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import java.math.BigDecimal

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val signatureValidator: SignatureValidator,
    private val userRepository: UserRepository,
    private val infuraApiService: InfuraApiService,
    private val authApiService: AuthApiService,
    @Lazy private val accountService: AccountService,
) {
    private final val logger = LoggerFactory.getLogger(this.javaClass)
    fun getWalletAccount(address: String, chainType: ChainType): Mono<WalletAccountResponse> {
        return findWallet(address, chainType)
            .next()
            .flatMap { wallet ->
                val account = accountService.findByAccountByWallet(wallet)
                Mono.zip(Mono.just(wallet.balance), account) { balance, accounts ->
                    WalletAccountResponse(balance, accounts)
                }
            }
    }

    @Transactional
    fun signInOrSignUp(request: ValidateRequest): Mono<SignInResponse> {
        return if (authenticateWallet(request)) {
            findOrCreateUserAndWallet(request.address,request.chain)
                .flatMap { wallet ->
                    getTokens(wallet)
                }
        } else {
            Mono.error(IllegalArgumentException("not valid Wallet Address"))
        }
    }

    fun getTokens(wallet: Wallet): Mono<SignInResponse> {
       return authApiService.getJwtToken(wallet.address)
            .map { jwt ->
                SignInResponse(wallet,jwt)
            }
    }

    fun findWallet(address: String, chain: ChainType?): Flux<Wallet> {
        return (if (chain != null) {
            walletRepository.findByAddressAndChainType(address, chain)
        } else {
            walletRepository.findAllByAddress(address)
        }).toFlux().switchIfEmpty(Flux.error(IllegalArgumentException("Wallet not found")))
    }


     fun findOrCreateUserAndWallet(address: String, chain: ChainType): Mono<Wallet> {
        return Mono.defer {
            walletRepository.findByAddressAndChainType(address, chain)
                .switchIfEmpty {
                    userRepository.save(Users(nickName = "Unknown"))
                        .flatMap { user ->
                            walletRepository.save(
                                Wallet(
                                    address = address,
                                    userId = user.id!!,
                                    chainType = chain,
                                    balance = BigDecimal.ZERO,
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        }
                }
                .flatMap { wallet ->
                    updateWalletBalance(wallet, wallet.chainType)
                }
        }
    }

    private fun authenticateWallet(request: ValidateRequest): Boolean {
        return signatureValidator.verifySignature(request)
    }

    private fun updateWalletBalance(wallet: Wallet,chainType: ChainType): Mono<Wallet> {
        return infuraApiService.getBalance(wallet.address, chainType)
            .map { wallet.updateBalance(it) }
            .flatMap { walletRepository.save(it)  }
    }
}