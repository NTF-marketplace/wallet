package com.api.wallet.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.rabbitmq")
class RabbitMQProperties {
    var queues: Map<String, String>? = null
    var template: TemplateProperties? = null

    class TemplateProperties {
        var exchange: String? = null
        var routingKey: String? = null
    }
}