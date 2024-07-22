package com.api.wallet.rabbitMQ

import com.api.wallet.rabbitMQ.dto.AdminTransferResponse
import com.api.wallet.rabbitMQ.dto.ListingResponse
import com.api.wallet.service.api.AccountService
import com.api.wallet.service.api.NftService
import com.api.wallet.service.external.nft.dto.NftResponse
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class MessageReceiver(
    private val accountService: AccountService,
    private val nftService: NftService,
) {
    @RabbitListener(bindings = [QueueBinding(
        value = Queue(name = "", durable = "false", exclusive = "true", autoDelete = "true"),
        exchange = Exchange(value = "transferExchange", type = ExchangeTypes.FANOUT)
    )])
    fun transferMessage(transfer: AdminTransferResponse) {
        accountService.saveAccount(transfer)
            .doOnSuccess { println("Account successfully saved") }
            .doOnError { error -> println("Error occurred: ${error.message}") }
            .subscribe()
    }

    @RabbitListener(bindings = [QueueBinding(
        value = Queue(name = "", durable = "false", exclusive = "true", autoDelete = "true"),
        exchange = Exchange(value = "nftExchange", type = ExchangeTypes.FANOUT)
    )])
    fun nftMessage(nft: NftResponse) {
        nftService.save(nft)
            .subscribe()
    }

    @RabbitListener(bindings = [QueueBinding(
        value = Queue(name = "", durable = "false", exclusive = "true", autoDelete = "true"),
        exchange = Exchange(value = "listingExchange", type = ExchangeTypes.FANOUT)
    )])
    fun listingMessage(listing: ListingResponse){
        println("active : " + listing.active)
        accountService.updateListing(listing).subscribe()
    }

    @RabbitListener(bindings = [QueueBinding(
        value = Queue(name = "", durable = "false", exclusive = "true", autoDelete = "true"),
        exchange = Exchange(value = "listingCancelExchange", type = ExchangeTypes.FANOUT)
    )])
    fun listingCancelMessage(listing: ListingResponse){
        accountService.updateListing(listing).subscribe()
    }


}