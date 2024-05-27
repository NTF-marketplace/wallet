package com.api.wallet.service.api

import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.controller.dto.response.SignInResponse
import com.api.wallet.domain.user.Users
import com.api.wallet.domain.user.repository.UserRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.ChainType
import com.api.wallet.service.external.auth.AuthApiService
import com.api.wallet.service.external.infura.InfuraApiService
import com.api.wallet.util.Util.convertNetworkTypeToChainType
import com.api.wallet.validator.SignatureValidator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val signatureValidator: SignatureValidator,
    private val userRepository: UserRepository,
    private val infuraApiService: InfuraApiService,
    private val authApiService: AuthApiService,
) {
    private final val logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun signInOrSignUp(request: ValidateRequest): Mono<SignInResponse> {
        return if (authenticateWallet(request)) {
            findOrCreateUserAndWallet(request)
                .flatMap { wallet ->
                    updateWalletBalance(wallet, request.chain)
                        .flatMap {
                            getTokens(it)
                        }
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

    fun findOrCreateUserAndWallet(request: ValidateRequest): Mono<Wallet> {
        return walletRepository.findByAddressAndChainType(request.address, request.chain)
                    .switchIfEmpty(Mono.defer {
                        createUserAndWallet(request.address, request.chain)
                    })

    }
    private fun createUserAndWallet(address: String, chain: ChainType): Mono<Wallet> {
        return userRepository.save(Users(nickName = "Unknown"))
                .flatMap { user ->
                    walletRepository.save(Wallet(
                        address = address,
                        userId = user.id!!,
                        chainType = chain,
                        balance = BigDecimal.ZERO,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis())
                    )
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