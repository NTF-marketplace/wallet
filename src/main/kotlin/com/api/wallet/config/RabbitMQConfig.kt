package com.api.wallet.config

import com.api.wallet.properties.RabbitMQProperties
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig(
    private val rabbitMQProperties: RabbitMQProperties,
) {
    private val logger = LoggerFactory.getLogger(RabbitMQConfig::class.java)

    @Bean
    fun nftQueue(): Queue {
        val queueName = rabbitMQProperties.queues?.get("nft")
            ?: throw IllegalAccessException("Queue name 'nft' not configured in properties.")
        return Queue(queueName, true)
    }

    @Bean
    fun exchange(): DirectExchange {
        return DirectExchange(rabbitMQProperties.template?.exchange!!)
    }

    @Bean
    fun binding(queue: Queue?, exchange: DirectExchange?): Binding {
        return BindingBuilder.bind(queue).to(exchange).with(rabbitMQProperties.template?.routingKey!!)
    }
}