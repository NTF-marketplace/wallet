package com.api.wallet

import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.domain.network.repository.NetworkRepository
import com.api.wallet.domain.user.repository.UserRepository
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.NetworkType
import com.api.wallet.service.api.WalletService
import com.api.wallet.service.api.WalletTransactionService
import com.api.wallet.service.infura.InfuraApiService
import com.api.wallet.service.moralis.MoralisService
import com.api.wallet.util.Util.toIsoString
import com.api.wallet.util.Util.toTimestamp
import com.api.wallet.validator.SignatureValidator
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.lang.System.currentTimeMillis


@SpringBootTest
//TODO("service 나눠서 작성")
class ValidatorTest(
    @Autowired private val signatureValidator: SignatureValidator,
    @Autowired private val infuraApiService: InfuraApiService,
    @Autowired private val moralisService: MoralisService,
    @Autowired private val walletService: WalletService,
    @Autowired private val networkRepository: NetworkRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val walletRepository: WalletRepository,
    @Autowired private val walletTransactionService: WalletTransactionService,
) {

    @Test
    fun SignatureValidatorRequestTest() {
        val request = ValidateRequest(
            address = "0x1234567890123456789012345678901234567890",
            message = "This is a test message",
            signature = "signature",
            network = NetworkType.POLYGON
        )

        if(request.isAddressValid()){
            println("true")
        }else{
            println("false")
        }
    }

    @Test
    fun validator() {
        val request = ValidateRequest(
            address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
            message = "Hello, MetaMask!",
            signature = "0x5714c3d6a6773a614091a9ac81dc8f4f6a0219349ddb4010edb6595c47b158814a9265e2c17aa7f7cfe479636a96c9f93cd665b213cc76b005c4b742edb6b27c1c",
            network = NetworkType.POLYGON
        )
        val isValid = signatureValidator.verifySignature(request)
        println(isValid)
    }

    @Test
    fun getBlockNumber() {
//        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val apiKey = "98b672d2ce9a4089a3a5cb5081dde2fa"
        val res = infuraApiService.getBlockNumber(ChainType.POLYGON_MAINNET)
        println(res.block())
    }


    @Test
    fun getBalance() {
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val apiKey = "98b672d2ce9a4089a3a5cb5081dde2fa"
        val res = infuraApiService.getBalance(address, ChainType.POLYGON_MAINNET)
        println("rest : " + res.block())
    }


    @Test
    fun getNftsByAddress() {
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val res= moralisService.getNFTsByAddress(address,ChainType.POLYGON_MAINNET)
        println("response : " + res.block())
    }

    @Test
    fun getWalletNFTTransfers() {
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val toDate = currentTimeMillis().toIsoString()
        val fromDate = 1710852978000.toIsoString()
        val res= moralisService.getWalletNFTTransfers(
            address,
            chainType = ChainType.POLYGON_MAINNET,
            toDate =toDate,
            fromDate =fromDate
        )
        println("response : " + res.block())
    }

    @Test
    fun signin() {
        val request = ValidateRequest(
            address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
            message = "Hello, MetaMask!",
            signature = "0x5714c3d6a6773a614091a9ac81dc8f4f6a0219349ddb4010edb6595c47b158814a9265e2c17aa7f7cfe479636a96c9f93cd665b213cc76b005c4b742edb6b27c1c",
            network = NetworkType.POLYGON
        )
        walletService.signInOrSignUp(request).block()
    }

    @Test
    fun tansferasdas() {
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        walletTransactionService.readAllTransactions(address).collectList().block()
    }

    @Test
    fun asd() {
        val res = "2024-03-19T12:56:07Z".toTimestamp()
        println(res)
    }


}