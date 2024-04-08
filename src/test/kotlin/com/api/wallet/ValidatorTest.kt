package com.api.wallet

import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.controller.dto.response.TransactionResponse
import com.api.wallet.domain.network.repository.NetworkRepository
import com.api.wallet.domain.transaction.Transaction
import com.api.wallet.domain.user.repository.UserRepository
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.NetworkType
import com.api.wallet.service.api.NftService
import com.api.wallet.service.api.WalletService
import com.api.wallet.service.api.WalletTransactionService
import com.api.wallet.service.external.infura.InfuraApiService
import com.api.wallet.service.external.moralis.MoralisApiService
import com.api.wallet.util.Util.toIsoString
import com.api.wallet.util.Util.toTimestamp
import com.api.wallet.validator.SignatureValidator
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import java.lang.System.currentTimeMillis


@SpringBootTest
//TODO("service 나눠서 작성")
class ValidatorTest(
    @Autowired private val signatureValidator: SignatureValidator,
    @Autowired private val infuraApiService: InfuraApiService,
    @Autowired private val moralisApiService: MoralisApiService,
    @Autowired private val walletService: WalletService,
    @Autowired private val networkRepository: NetworkRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val walletRepository: WalletRepository,
    @Autowired private val walletTransactionService: WalletTransactionService,
    @Autowired private val nftService: NftService,
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
        val res= moralisApiService.getNFTsByAddress(address,ChainType.POLYGON_MAINNET)
        println("response : " + res.block())
    }

    @Test
    fun getWalletNFTTransfers() {
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val toDate = currentTimeMillis().toIsoString()
        val fromDate = 1710852978000.toIsoString()
        val res= moralisApiService.getWalletNFTTransfers(
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
        val response  = walletService.signInOrSignUp(request).block()

        println(response?.wallet?.balance)
        println(response?.tokens?.accessToken)
        println(response?.tokens?.refreshToken)
    }

    @Test
    fun tansferasdas() {
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val pagebale = PageRequest.of(0,20)
        val networkType = NetworkType.POLYGON

        val transaction: Page<TransactionResponse>? = walletTransactionService.readAllTransactions(address,networkType,pagebale).block()
            println("total element : "+transaction?.totalElements)
            println("totalPages : " + transaction?.totalPages)
        transaction?.content?.forEach {
            println(it.blockTimestamp)
            println(it.nft.id)
            println(it.nft.image)
            println(it.nft.tokenAddress)
            }
    }

    @Test
    fun asd() {
        val res = "2024-03-19T12:56:07Z".toTimestamp()
        println(res)
    }

    @Test
    fun readAllNfts() {
        val address = "0x01b72b4aa3f66f213d62d53e829bc172a6a72867"
        val pagebale = PageRequest.of(0,13)
        val nftList= nftService.readAllNftByWallet(address,null,pagebale).block()

        nftList?.content?.map {
            println(it.tokenId)
            println(it.nftName)
            println(it.tokenAddress)
            println(it.id)
            println(it.collectionName)
            println("------------------")
        }

    }


}