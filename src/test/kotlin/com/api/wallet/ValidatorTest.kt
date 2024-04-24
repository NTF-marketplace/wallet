package com.api.wallet

import com.api.wallet.controller.WalletController
import com.api.wallet.controller.dto.request.ValidateRequest
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.NetworkType
import com.api.wallet.service.api.NftService
import com.api.wallet.service.api.WalletService
import com.api.wallet.service.api.WalletTransactionService
import com.api.wallet.service.external.infura.InfuraApiService
import com.api.wallet.service.external.moralis.MoralisApiService
import com.api.wallet.validator.SignatureValidator
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.web3j.utils.Numeric
import java.math.BigInteger

@SpringBootTest
//TODO("service 나눠서 작성")
class ValidatorTest(
    @Autowired private val signatureValidator: SignatureValidator,
    @Autowired private val infuraApiService: InfuraApiService,
    @Autowired private val moralisApiService: MoralisApiService,
    @Autowired private val walletService: WalletService,
    @Autowired private val nftService: NftService,
    @Autowired private val walletController: WalletController,
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
            network = NetworkType.POLYGON
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


    @Test
    fun getTransactionTest() {
       //walletTransactionService.getTransactionERC721("0xBd3531dA5CF5857e7CfAA92426877b022e612cf8","151")

        walletTransactionService.getTransactionERC1155("0x495f947276749Ce646f68AC8c248420045cb7b5e","72639390708267126639436568569652760193401123475841185469851730231265004093441")
        // val tokenId = "151"
        // val tokenIdBI = Numeric.toBigInt(tokenId)
        // val topicTokenId = Numeric.toHexStringWithPrefixZeroPadded(tokenIdBI, 64)
        // println("Converted tokenId: $topicTokenId")
        // walletTransactionService.fetchLogsFromInfura()
    }

    @Test
    fun hex16() {
        val tokenId = "151"
        val res = Numeric.toHexStringWithPrefixZeroPadded(BigInteger(tokenId), 64)


        println(res)
        var i = 0;
        res.map {
            i++
        }

        println(i)



    }



}