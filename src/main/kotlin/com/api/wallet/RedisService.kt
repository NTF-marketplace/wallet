package com.api.wallet

import com.api.wallet.controller.dto.response.NftMetadataResponse
import com.api.wallet.service.external.nft.NftApiService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisClusterConnection
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class RedisService(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
    private val nftApiService: NftApiService,
    private val lettuceConnectionFactory: LettuceConnectionFactory
) {
    private val logger: Logger = LoggerFactory.getLogger(RedisService::class.java)
    fun getNft(nftId: Long): Mono<NftMetadataResponse> {
        return reactiveRedisTemplate.opsForValue().get("NFT:$nftId")
            .map { data ->
                objectMapper.convertValue(data, NftMetadataResponse::class.java)
            }.doOnNext {
                logRetrievalInfo("NFT:$nftId")
            }
            .switchIfEmpty {
                nftApiService.getNftById(nftId)
            }
    }



    fun getNfts(nftIds: List<Long>): Flux<NftMetadataResponse> {
        if (nftIds.isEmpty()) {
            return Flux.empty()
        }
        val keys = nftIds.map { "NFT:$it" }
        return reactiveRedisTemplate.opsForValue().multiGet(keys)
            .flatMapMany { list ->
                Flux.fromIterable(list.filterNotNull().map { data ->
                    objectMapper.convertValue(data, NftMetadataResponse::class.java)
                }).switchIfEmpty (
                    nftApiService.getNftsByIds(nftIds)
                )
            }
    }
    private fun logRetrievalInfo(key: String) {
        val connection = lettuceConnectionFactory.connection
        when (connection) {
            is RedisClusterConnection -> {
                val actualNode = connection.clusterGetNodeForKey(key.toByteArray())
                logger.info("Actual node used for key $key: ${actualNode.host}:${actualNode.port}")
            }
        }
    }
}