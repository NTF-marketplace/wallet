package com.api.wallet.service.api

import com.api.wallet.domain.nft.repository.NftRepository
import com.api.wallet.domain.transaction.repository.TransactionRepository
import com.api.wallet.domain.wallet.repository.WalletRepository
import com.api.wallet.service.external.moralis.MoralisApiService
import com.api.wallet.service.external.nft.NftApiService
import org.springframework.stereotype.Service
import org.web3j.abi.EventEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bytes
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.math.BigInteger


@Service
class WalletTransactionService(
    private val moralisApiService: MoralisApiService,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val nftService: NftService,
    private val nftRepository: NftRepository,
    private val nftApiService: NftApiService,
) {


    private val  web3 = Web3j.build(HttpService("https://mainnet.infura.io/v3/98b672d2ce9a4089a3a5cb5081dde2fa"))
    private val  web3Polygon = Web3j.build(HttpService("https://polygon-mainnet.infura.io/v3/98b672d2ce9a4089a3a5cb5081dde2fa"))


    fun getTransactionERC721(contractAddress: String, tokenId: String) {
       // val tokenId = BigInteger(tokenId)

        val transferEvent = Event(
            "Transfer",
            listOf(
                TypeReference.create(Address::class.java, true),
                TypeReference.create(Address::class.java, true),
                TypeReference.create(Uint256::class.java, true),
            )
        )

        val eventSignature = EventEncoder.encode(transferEvent)
        //val tokenIdHex = tokenId.toString(16).padStart(64, '0')

        val filter = EthFilter(
            DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST,
            contractAddress
        ).addSingleTopic(eventSignature)
            .addSingleTopic(null)
            .addSingleTopic(null)
            .addSingleTopic(Numeric.toHexStringWithPrefixZeroPadded(BigInteger(tokenId), 64))

        try {
            // 로그 가져오기
            val logs = web3.ethGetLogs(filter).send()
            logs.logs.forEach {
                val log = it.get() as Log
                val (from, to, tokenId) = parseTransferEvent(log)
                println("From: $from, To: $to, TokenID: $tokenId")
            }
            //println(logs.logs)

        } catch (e: Exception) {
            println("Error fetching logs: ${e.message}")
        }
    }


    fun parseTransferEvent(log: Log): Triple<String, String, BigInteger> {
        val fromAddress = "0x" + log.topics[1].substring(log.topics[1].length - 40)
        val toAddress = "0x" + log.topics[2].substring(log.topics[2].length - 40)
        val tokenId = Numeric.toBigInt(log.topics[3])

        return Triple(fromAddress, toAddress, tokenId)
    }



    fun getTransactionERC1155(address: String, tokenId: String) {

        val transferSingleEvent = Event(
            "TransferSingle",
            listOf(
                TypeReference.create(Address::class.java),
                TypeReference.create(Address::class.java),
                TypeReference.create(Address::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java)
            )
        )

        val filter = EthFilter(
            DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST,
            address
        ).addSingleTopic(EventEncoder.encode(transferSingleEvent))
            .addSingleTopic(null) // operator 주소
            .addSingleTopic(null) // from 주소
            .addSingleTopic(null) // to 주소
            .addSingleTopic(Numeric.toHexStringWithPrefixZeroPadded(BigInteger(tokenId), 64))
            .addSingleTopic(null)

        val res =web3.ethGetLogs(filter).send()
        res.logs.forEach {
            println(it.toString())
        }

    }




//    @Transactional
//    fun readAllTransactions(address: String, networkType: NetworkType?, pageable: Pageable): Mono<Page<TransactionResponse>> {
//        val wallets = if (networkType != null) {
//            walletRepository.findByAddressAndNetworkType(address, networkType.toString()).flux()
//        } else {
//            walletRepository.findAllByAddress(address)
//        }
//
//        return wallets
//            .flatMap { wallet ->
//                getTransactions(wallet, pageable)
//            }
//            .collectList()
//            .flatMap { transactions ->
//                Flux.fromIterable(transactions)
//                    .flatMap { transaction ->
//                        getNftResponse(transaction)
//                    }
//                    .collectList()
//                    .zipWith(transactionRepository.count())
//                    .map { PageImpl(it.t1, pageable, it.t2) }
//            }
//    }
//
//    private fun getNftResponse(transaction: Transaction): Mono<TransactionResponse> {
//        return nftRepository.findById(transaction.nftId)
//            .flatMap { nft ->
//                val toBatchRequest = nftService.toBatchRequest(listOf(nft))
//                nftApiService.getNftBatch(toBatchRequest).next()
//            }
//            .map { nftResponse ->
//                TransactionResponse(
//                    toAddress = transaction.toAddress,
//                    fromAddress = transaction.fromAddress,
//                    amount = transaction.amount,
//                    value = transaction.value,
//                    blockTimestamp = transaction.blockTimestamp,
//                    walletId = transaction.walletId,
//                    nft = nftResponse
//                )
//            }
//    }
//
//    private fun getTransactions(wallet: Wallet, pageable: Pageable): Flux<Transaction> {
//        val updateFlux = transactionRepository.findAllByWalletIdOrderByBlockTimestampDesc(wallet.id!!,pageable)
//            .collectList()
//            .flatMapMany {
//                val lastBlockTimestamp = it.firstOrNull()?.blockTimestamp?.plus(10000)
//                moralisApiService.getWalletNFTTransfers(
//                    wallet.address,
//                    wallet.networkType.convertNetworkTypeToChainType(),
//                    lastBlockTimestamp?.toIsoString() ?: null,
//                    System.currentTimeMillis().toIsoString()
//                )
//                    .flatMapMany { Flux.fromIterable(it.result) }
//                    .filter { !it.possibleSpam }
//                    .flatMap { result ->
//                        saveOrUpdate(Flux.just(result), wallet)
//                    }
//            }
//
//        return updateFlux.thenMany(
//            transactionRepository.findAllByWalletIdOrderByBlockTimestampDesc(wallet.id!!, pageable)
//        )
//    }
//
//
//
//    private fun saveOrUpdate(results: Flux<TransferResult>, wallet: Wallet): Flux<Transaction> {
//       return results.flatMap {result ->
//           nftService.findOrCreateNft(result.tokenAddress,wallet.networkType,result.tokenId).flatMap { nft->
//               transactionRepository.save(
//                   Transaction(
//                       nftId = nft.id!!,
//                       toAddress = result.toAddress,
//                       fromAddress = result.fromAddress,
//                       amount = result.amount.toInt(),
//                       value = result.value.toBigDecimal(),
//                       hash =  result.blockHash,
//                       blockTimestamp = result.blockTimestamp.toTimestamp(),
//                       walletId = wallet.id!!
//                   )
//               )
//           }
//        }.collectList()
//           .flatMapMany { Flux.fromIterable(it) }
//    }

}