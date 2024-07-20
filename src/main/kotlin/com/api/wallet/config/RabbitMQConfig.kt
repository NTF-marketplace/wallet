package com.api.wallet.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {
    @Bean
    fun jsonMessageConverter(): Jackson2JsonMessageConverter = Jackson2JsonMessageConverter()

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory, jsonMessageConverter: Jackson2JsonMessageConverter): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = jsonMessageConverter
        return template
    }

    private fun createQueue(name: String, durable: Boolean = true): Queue {
        return Queue(name, durable)
    }

    private fun createExchange(name: String): DirectExchange {
        return DirectExchange(name)
    }

    private fun createBinding(queue: Queue, exchange: DirectExchange, routingKey: String): Binding {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey)
    }

    @Bean
    fun transferQueue() = createQueue("transferQueue")

    @Bean
    fun transferExchange() = createExchange("transferExchange")

    @Bean
    fun bindingTransferQueue(transferQueue: Queue, transferExchange: DirectExchange) = createBinding(transferQueue, transferExchange, "transferRoutingKey")

    @Bean
    fun nftQueue() = createQueue("nftQueue")

    @Bean
    fun nftExchange() = createExchange("nftExchange")

    @Bean
    fun bindingNftQueue(nftQueue: Queue, nftExchange: DirectExchange) = createBinding(nftQueue, nftExchange, "nftRoutingKey")


    @Bean
    fun listingQueue() = createQueue("listingQueue")

    @Bean
    fun listingExchange() = createExchange("listingExchange")

    @Bean
    fun bindingListingQueue(listingQueue: Queue, listingExchange: DirectExchange) = createBinding(listingQueue, listingExchange, "listingRoutingKey")
}