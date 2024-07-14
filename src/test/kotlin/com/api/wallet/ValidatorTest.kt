package com.api.wallet

import com.api.wallet.controller.WalletController
import com.api.wallet.controller.dto.request.DepositRequest
import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.controller.dto.request.WithdrawERC20Request
import com.api.wallet.domain.TestRepository
import com.api.wallet.domain.account.log.AccountLogRepository
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.AccountType
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.MyEnum
import com.api.wallet.enums.TransferType
import com.api.wallet.rabbitMQ.dto.AdminTransferResponse
import com.api.wallet.service.api.AccountService
import com.api.wallet.service.api.NftService
import com.api.wallet.service.api.WalletService
import com.api.wallet.service.external.admin.AdminApiService
import com.api.wallet.service.external.infura.InfuraApiService
import com.api.wallet.service.external.moralis.MoralisApiService
import com.api.wallet.validator.SignatureValidator
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.math.BigInteger
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
    @Autowired private val redisService: RedisService,
    @Autowired private val adminApiService: AdminApiService,
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
        val res = infuraApiService.getBlockNumber(ChainType.POLYGON_MAINNET)
        println(res.block())
    }


    @Test
    fun getBalance() {
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val res = infuraApiService.getBalance(address, ChainType.POLYGON_MAINNET)
        println("rest : " + res.block())
    }


    @Test
    fun getNftsByAddress() {
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val res= moralisApiService.getNFTsByAddress(address,ChainType.POLYGON_MAINNET)
        println("response : " + res.block())
    }

    @Test
    fun signin1() {
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
    fun signin2() {
        val request = ValidateRequest(
            address = "0x9bDeF468ae33b09b12a057B4c9211240D63BaE65",
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
        val page = PageRequest.of(0,10)
        val response = walletController.getAllNft(null,address,page).block()

        response?.content?.map {
            println(it.id)
            println(it.tokenAddress)
        }

    }

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
            id= 3L,
            walletAddress = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
            nftId = null,
            timestamp =  Instant.now().toEpochMilli(),
            accountType = AccountType.DEPOSIT,
            transferType = TransferType.ERC20,
            balance = BigDecimal(0.8),
            chainType = ChainType.POLYGON_MAINNET
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

//    @Test
//    fun findAllAccountByAddress() {
//        val walletAddress = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
//        accountService.findByAccountByAddress(walletAddress)
//    }
    
    @Test
    fun redistest() {
        val res = redisService.getNft(3L).block()
        println(res.toString())
    }

    @Test
    fun testasd(){
        val test = com.api.wallet.domain.Test(
            null,
            MyEnum.APPLE
        )
        testRepository.save(test).block()
    }

    @Test
    fun depositTest() {
        val req = DepositRequest(
            chainType = ChainType.POLYGON_MAINNET,
            transactionHash = "0xa7c770b9595177528c26f74ffe99d650cf8dd6a9345b14bb457d9348087588ab"
        )

        accountService.depositProcess(
            address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867",
            request = req
        ).block()

        Thread.sleep(8000)
    }


    @Test
    fun withdrawERC20(){
        val req = WithdrawERC20Request(
            chainType = ChainType.POLYGON_AMOY,
            amount = BigDecimal("0.5")
        )

        accountService.withdrawERC20Process(address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867" , req)
            .block()

        Thread.sleep(5000)

    }



}