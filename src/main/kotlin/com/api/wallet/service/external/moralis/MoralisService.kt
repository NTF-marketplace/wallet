package com.api.wallet.service.external.moralis

import com.api.wallet.enums.ChainType
import com.api.wallet.service.external.moralis.dto.response.NFTByWalletResponse
import com.api.wallet.service.external.moralis.dto.response.NFTTransferByWallet
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
//TODO("moralis apiKey properties에 저장 gitignore)
class MoralisService {

    private val webClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build()

    private fun queryParamByChain(chain: ChainType): String? {
        val chain = when (chain) {
            ChainType.ETHEREUM_MAINNET -> "eth"
            ChainType.POLYGON_MAINNET -> "polygon"
            ChainType.ETHREUM_GOERLI -> "goerli"
            ChainType.POLYGON_MUMBAI -> "munbai"
            ChainType.ETHREUM_SEPOLIA -> "sepolia"
        }
        return chain
    }

    fun getNFTsByAddress(walletAddress: String,chainType: ChainType): Mono<NFTByWalletResponse> {
        val chain = queryParamByChain(chainType)
        return webClient.get()
            .uri {
                it.path("/v2.2/${walletAddress}/nft")
                it.queryParam("chain", chain)
                it.queryParam("exclude_spam", true)
                it.build()
            }
            .header("X-API-Key", apiKey)
            .header("Accept", MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(NFTByWalletResponse::class.java)
    }

    fun getWalletNFTTransfers(
        walletAddress: String,
        chainType: ChainType,
        fromDate: String?,
        toDate: String?,
    ): Mono<NFTTransferByWallet> {
        val chain =queryParamByChain(chainType)
        return webClient.get()
            .uri {
                it.path("/v2.2/${walletAddress}/nft/transfers")
                it.queryParam("chain",chain)
                    .apply {
                        if(fromDate!=null){
                            it.queryParam("from_date",fromDate)
                        }
                        if(toDate != null) {
                            it.queryParam("to_date",toDate)
                        }
                    }
                it.build()
            }
            .header("X-API-Key", apiKey)
            .header("accept",MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToMono(NFTTransferByWallet::class.java)
    }

    companion object {
        private val apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJub25jZSI6ImJiNmIxMWJmLWNmNzItNDg0OC04OGEyLTBjYTIwODRjN2VhMyIsIm9yZ0lkIjoiMzgzODQwIiwidXNlcklkIjoiMzk0NDAyIiwidHlwZUlkIjoiMGZlYWQ5NDctZjQwZS00MDkwLWFlNGUtOTA1ZTdmMjUxZTAzIiwidHlwZSI6IlBST0pFQ1QiLCJpYXQiOjE3MTA5NTIwMjYsImV4cCI6NDg2NjcxMjAyNn0.VQE60IPGiWxdp7jKLF0jzXnxrLjEpU56H4bnfhMt0Sw"
        private val baseUrl = "https://deep-index.moralis.io/api"
    }
}