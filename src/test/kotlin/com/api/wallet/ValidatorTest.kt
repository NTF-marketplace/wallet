package com.api.wallet

import com.api.wallet.controller.WalletController
import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.domain.TestRepository
import com.api.wallet.domain.account.log.AccountLogRepository
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.AccountType
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.MyEnum
import com.api.wallet.enums.TransferType
import com.api.wallet.rabbitMQ.dto.AdminTransferResponse
import com.api.wallet.service.api.AccountLogService
import com.api.wallet.service.api.AccountService
import com.api.wallet.service.api.NftService
import com.api.wallet.service.api.WalletService
import com.api.wallet.service.external.infura.InfuraApiService
import com.api.wallet.service.external.moralis.MoralisApiService
import com.api.wallet.validator.SignatureValidator
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier
import java.time.Instant

@SpringBootTest
//TODO("service 나눠서 작성")
class ValidatorTest(
    @Autowired private val signatureValidator: SignatureValidator,
    @Autowired private val infuraApiService: InfuraApiService,
    @Autowired private val moralisApiService: MoralisApiService,
    @Autowired private val walletService: WalletService,
    @Autowired private val nftService: NftService,
    @Autowired private val walletController: WalletController,
    @Autowired private val accountService: AccountService,
    @Autowired private val walletRepository: WalletRepository,
    @Autowired private val accountLogRepository: AccountLogRepository,
    @Autowired private val testRepository: TestRepository,
) {

    @Test
    fun SignatureValidatorRequestTest() {
        val request = ValidateRequest(
            address = "0x1234567890123456789012345678901234567890",
            message = "This is a test message",
            signature = "signature",
            chain = ChainType.POLYGON_MAINNET
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
            chain = ChainType.POLYGON_MAINNET
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
        val res= moralisApiService.getNFTsByAddress(address,ChainType.POLYGON_MAINNET)
        println("response : " + res.block())
    }

//    @Test
//    fun getWalletNFTTransfers() {
//        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
//        val toDate = currentTimeMillis().toIsoString()
//        val fromDate = 1710852978000.toIsoString()
//        val res= moralisApiService.getWalletNFTTransfers(
//            address,
//            chainType = ChainType.POLYGON_MAINNET,
//            toDate =toDate,
//            fromDate =fromDate
//        )
//        println("response : " + res.block())
//    }

    @Test
    fun signin() {
        val request = ValidateRequest(
            address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
            message = "Hello, MetaMask!",
            signature = "0x5714c3d6a6773a614091a9ac81dc8f4f6a0219349ddb4010edb6595c47b158814a9265e2c17aa7f7cfe479636a96c9f93cd665b213cc76b005c4b742edb6b27c1c",
            chain = ChainType.POLYGON_MAINNET
        )
        val response  = walletService.signInOrSignUp(request).block()

        println(response?.wallet?.balance)
        println(response?.tokens?.accessToken)
        println(response?.tokens?.refreshToken)
    }

    @Test
    fun readAllNfts() {
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
//        val nftList= nftService.readAllNftByWallet(address,null).blockLast()

        val response = walletController.readAllNftByWallet(null,address).block()

        println("status : " + response?.statusCode)
        response?.body?.map {
            println(it.nftName)
            println(it.id)
            println(it.tokenAddress)
        }
    }


    // @Test
    // fun saveAccountNfts() {
    //     val response = AdminTransferResponse(
    //         id = 4L,
    //         walletAddress = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
    //         nftId = 4L,
    //         timestamp = 1716192591L,
    //         accountType = "DEPOSIT"
    //     )
    //
    //     accountService.saveAccountNfts(response).block()
    //     Thread.sleep(10000)
    // }

    @Test
    fun testFindWalletByAddress() {
        val walletAddress = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val wallet = walletRepository.findAllByAddress(walletAddress).next().block()
        assertNotNull(wallet)
        println("Wallet found: ${wallet?.address}")
    }

    @Test
    fun test2() {
        val response = AdminTransferResponse(
            id= 1L,
            walletAddress = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
            nftId = 1L,
            timestamp =  Instant.now().toEpochMilli(),
            accountType = AccountType.DEPOSIT,
            transferType = TransferType.ERC721,
            balance = null
        )
        accountService.saveAccount(response).block()

        Thread.sleep(100000)
    }

    @Test
    fun getAccountLog() {
        val account = accountLogRepository.findById(1L).block()
        println(account?.accountId)
        println(account?.accountType)
    }


    @Test
    fun findByChainType() {
        val result = walletRepository.findByChainType(ChainType.POLYGON_MAINNET)

        StepVerifier.create(result)
            .expectNextMatches { it.chainType == ChainType.POLYGON_MAINNET }
            .verifyComplete()
    }

    @Test
    fun enumTest() {
        val result = testRepository.findByType(MyEnum.APPLE)
        StepVerifier.create(result)
            .expectNextMatches { it.type == MyEnum.APPLE }
            .verifyComplete()
    }

    @Test
    fun enumTest_save() {
        val test= com.api.wallet.domain.Test(
            id= null,
            type = MyEnum.APPLE
        )
        testRepository.save(test).block()
    }
//    @Test
//    fun enumTest() {
//        val result = accountLogRepository.findAllByAccountType(AccountType.DEPOSIT)
//        StepVerifier.create(result)
//            .expectNextMatches { it.accountType == AccountType.DEPOSIT }
//            .verifyComplete()
//    }
}