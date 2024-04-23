package com.api.wallet.config

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {
    @Bean
    fun nftQueue(): Queue {
        return Queue("nftQueue", true)
    }

    @Bean
    fun nftExchange(): DirectExchange {
        return DirectExchange("nftExchange")
    }

    @Bean
    fun bindingNftQueue(nftQueue: Queue, nftExchange: DirectExchange): Binding {
        return BindingBuilder.bind(nftQueue).to(nftExchange).with("nftRoutingKey")
    }


}