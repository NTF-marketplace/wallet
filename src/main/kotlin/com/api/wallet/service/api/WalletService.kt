package com.api.wallet.service.api

import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.domain.network.Network
import com.api.wallet.domain.user.Users
import com.api.wallet.domain.user.repository.UserRepository
import com.api.wallet.domain.wallet.Wallet
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.NetworkType
import com.api.wallet.service.infura.InfuraApiService
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
) {
    private final val logger = LoggerFactory.getLogger(this.javaClass)

    //TODO("반환값은 wallet과 jwt")
    @Transactional
    fun signInOrSignUp(request: ValidateRequest): Mono<Wallet> {
        return if (authenticateWallet(request)) {
            networkService.findByType(request.network)
                .flatMap { network ->
                    walletRepository.findByAddressAndNetworkType(request.address, network.type!!)
                        .switchIfEmpty(Mono.defer {
                            createUserAndWallet(request.address, network)
                        })
                }
                .flatMap { wallet ->
                    updateWalletBalance(wallet, request.network)
//                        .flatMap { updatedWallet ->
//                        requestJwt(wallet.address).map { jwt ->
//                            Pair(updatedWallet, jwt)
//                        }
//                    }
                }
        } else {
            Mono.error(IllegalArgumentException("유효하지 않는 지갑 주소"))
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