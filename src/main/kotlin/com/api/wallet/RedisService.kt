package com.api.wallet

import com.api.wallet.controller.dto.response.NftMetadataResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class RedisService(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    private  val objectMapper: ObjectMapper
) {

    fun getNft(nftId: Long): Mono<NftMetadataResponse> {
        return reactiveRedisTemplate.opsForValue().get("NFT:$nftId")
            .map { data ->
                objectMapper.convertValue(data, NftMetadataResponse::class.java)
            }
    }
    // fun getNft(nftId: Long): Mono<NftMetadataResponse> {
    //     return reactiveRedisTemplate.opsForValue().get("NFT:$nftId")
    //         .map { data ->
    //             objectMapper.convertValue(data, NftMetadataResponse::class.java)
    //         }
    // }
}