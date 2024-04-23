package com.api.wallet.rabbitMQ

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class MessageReceiver {

    @RabbitListener(queues = ["nftQueue"])
    fun receiveMessage(nftId: Long) {
        println("Received Message: $nftId")
    }
}