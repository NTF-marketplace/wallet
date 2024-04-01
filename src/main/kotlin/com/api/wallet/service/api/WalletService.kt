package com.api.wallet.service.api

import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.controller.dto.response.SignInResponse
import com.api.wallet.domain.network.Network
import com.api.wallet.domain.user.Users
import com.api.wallet.domain.user.repository.UserRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.NetworkType
import com.api.wallet.service.external.auth.AuthApiService
import com.api.wallet.service.external.infura.InfuraApiService
import com.api.wallet.util.Util.convertNetworkTypeToChainType
import com.api.wallet.validator.SignatureValidator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.math.BigDecimal
import kotlin.concurrent.thread

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val signatureValidator: SignatureValidator,
    private val userRepository: UserRepository,
    private val networkService: NetworkService,
    private val infuraApiService: InfuraApiService,
    private val authApiService: AuthApiService,
) {
    private final val logger = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun signInOrSignUp(request: ValidateRequest): Mono<SignInResponse> {
        return if (authenticateWallet(request)) {
            findOrCreateUserAndWallet(request)
                .flatMap { wallet ->
                    updateWalletBalance(wallet, request.network)
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
        return networkService.findByType(request.network)
            .flatMap { network ->
                walletRepository.findByAddressAndNetworkType(request.address, network.type!!)
                    .switchIfEmpty(Mono.defer {
                        createUserAndWallet(request.address, network)
                    })
            }
    }


    private fun createUserAndWallet(address: String, network: Network): Mono<Wallet> {
        return userRepository.save(Users(nickName = "Unknown"))
                .flatMap { user ->
                    walletRepository.save(Wallet(
                        address = address,
                        userId = user.id!!,
                        networkType = network.type!!,
                        balance = BigDecimal.ZERO,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis())
                    )
                }
    }

    private fun authenticateWallet(request: ValidateRequest): Boolean {
        return signatureValidator.verifySignature(request)
    }

    private fun updateWalletBalance(wallet: Wallet,networkType: NetworkType): Mono<Wallet> {
        val chainType = networkType.toString().convertNetworkTypeToChainType()
        return infuraApiService.getBalance(wallet.address, chainType)
            .map { wallet.updateBalance(it) }
            .flatMap { walletRepository.save(it)  }
    }
}