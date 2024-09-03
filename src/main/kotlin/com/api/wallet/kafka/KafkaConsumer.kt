package com.api.wallet.kafka

import com.api.wallet.kafka.dto.SaleResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.Message
import org.springframework.stereotype.Service

@Service
class KafkaConsumer(
    private val objectMapper: ObjectMapper,
) {
    @KafkaListener(topics = ["sale-topic"],
        groupId = "wallet-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consumeLedgerStatusEvents(message: Message<Any>) {
        val payload = message.payload

        if (payload is LinkedHashMap<*, *>) {
            val saleStatusRequest = objectMapper.convertValue(payload, SaleResponse::class.java)

            println("saleStatusRequest : " + saleStatusRequest)
            // orderService.updateOrderStatus(ledgerStatusRequest).subscribe()
        }
    }

}