package com.api.wallet.rabbitMQ

import com.api.wallet.rabbitMQ.dto.AdminTransferResponse
import com.api.wallet.rabbitMQ.dto.ListingResponse
import com.api.wallet.service.api.AccountService
import com.api.wallet.service.api.NftService
import com.api.wallet.service.external.nft.dto.NftResponse
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class MessageReceiver(
    private val accountService: AccountService,
    private val nftService: NftService,
) {
    @RabbitListener(queues = ["transferQueue"])
    fun depositMessage(transfer: AdminTransferResponse) {
        accountService.saveAccount(transfer)
            .doOnSuccess { println("Account successfully saved") }
            .doOnError { error -> println("Error occurred: ${error.message}") }
            .subscribe()
    }

    @RabbitListener(queues = ["nftQueue"])
    fun nftMessage(nft: NftResponse) {
        println("data1??")
        nftService.save(nft)
            .subscribe()
    }

    @RabbitListener(queues = ["listingQueue"])
    fun listingMessage(listing: ListingResponse){
        // nftListingService.update(listing).subscribe()
    }

}