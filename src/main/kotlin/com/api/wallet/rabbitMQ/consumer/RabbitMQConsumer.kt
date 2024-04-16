package com.api.wallet.rabbitMQ.consumer

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class RabbitMQConsumer {

    @RabbitListener(queues = [NFT_QUEUE])
    fun receiveNft(nftId: Long) {
        println("Received nft: $nftId")
    }

    companion object {
        const val NFT_QUEUE = "nftQueue"
    }
}