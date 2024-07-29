package com.api.wallet.util

import com.api.wallet.domain.nft.Nft
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.TokenType
import com.api.wallet.service.external.nft.dto.NftResponse
import com.google.gson.Gson
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.format.DateTimeFormatter

object Util {

    fun ChainType.toTokenType(): TokenType {
        return when (this) {
            ChainType.ETHEREUM_MAINNET -> TokenType.ETH
            ChainType.POLYGON_MAINNET -> TokenType.MATIC
            ChainType.POLYGON_AMOY -> TokenType.MATIC
            ChainType.ETHEREUM_SEPOLIA -> TokenType.ETH
            ChainType.ETHEREUM_HOLESKY -> TokenType.ETH
            else -> throw IllegalArgumentException("Unknown ChainType: $this")
        }
    }

    fun <T> toPage(flux: Flux<T>, pageable: Pageable, countMono: Mono<Long>): Mono<Page<T>> {
        return flux.collectList()
            .zipWith(countMono)
            .map { tuple ->
                val content = tuple.t1
                val total = tuple.t2
                PageImpl(content, pageable, total)
            }
    }

    fun <T> toPagedMono(flux: Flux<T>, pageable: Pageable): Mono<Page<T>> {
        return flux.collectList()
            .map { list ->
                val start = pageable.offset.toInt()
                val end = (start + pageable.pageSize).coerceAtMost(list.size)
                val page = if (start <= end) list.subList(start, end) else listOf()
                PageImpl(page, pageable, list.size.toLong())
            }
    }

    fun toNftResponse(nft: Nft): NftResponse {
        return NftResponse(
            id = nft.id,
            tokenId = nft.tokenId,
            tokenAddress = nft.tokenAddress,
            chainType = nft.chainType
        )
    }
}