package com.api.wallet.rabbitMQ

import com.api.wallet.rabbitMQ.dto.AdminTransferResponse
import com.api.wallet.service.api.AccountService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class MessageReceiver(
    private val accountService: AccountService,
) {
    @RabbitListener(queues = ["transferQueue"])
    fun depositMessage(transfer: AdminTransferResponse) {
        accountService.saveAccountNfts(transfer)
    }
}