package com.api.wallet.rabbitMQ

import com.api.wallet.rabbitMQ.dto.AdminTransferResponse
import com.api.wallet.rabbitMQ.dto.AuctionResponse
import com.api.wallet.rabbitMQ.dto.ListingResponse
import com.api.wallet.service.api.AccountService
import com.api.wallet.service.api.NftService
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class MessageReceiver(
    private val accountService: AccountService,
) {

    @RabbitListener(bindings = [QueueBinding(
        value = Queue(name = "", durable = "false", exclusive = "true", autoDelete = "true"),
        exchange = Exchange(value = "transferExchange", type = ExchangeTypes.FANOUT)
    )], ackMode = "MANUAL")
    fun transferMessage(
        transfer: AdminTransferResponse,
        channel: Channel,
        @Header(AmqpHeaders.DELIVERY_TAG) deliveryTag: Long
    ) {
        try {
            accountService.processTransfer(transfer).subscribe()
            channel.basicAck(deliveryTag, false)
        } catch (e: Exception) {
            channel.basicNack(deliveryTag, false, true)
        }
    }

        @RabbitListener(bindings = [QueueBinding(
        value = Queue(name = "", durable = "false", exclusive = "true", autoDelete = "true"),
        exchange = Exchange(value = "listingExchange", type = ExchangeTypes.FANOUT)
    )])
    fun listingMessage(listing: ListingResponse){
        accountService.updateListing(listing).subscribe()
    }

     @RabbitListener(bindings = [QueueBinding(
         value = Queue(name = "", durable = "false", exclusive = "true", autoDelete = "true"),
         exchange = Exchange(value = "auctionExchange", type = ExchangeTypes.FANOUT)
     )])
     fun auctionMessage(auction: AuctionResponse){
         accountService.updateAuction(auction).subscribe()
     }


}