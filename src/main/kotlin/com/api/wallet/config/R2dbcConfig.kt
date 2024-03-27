package com.api.wallet.config

import com.api.wallet.enums.NetworkType
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.codec.EnumCodec
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager


@Configuration
@EnableR2dbcRepositories
class R2dbcConfig : AbstractR2dbcConfiguration(){


    @Bean
    override fun connectionFactory(): PostgresqlConnectionFactory {
        val configuration = PostgresqlConnectionConfiguration.builder()
            .host("localhost")
            .database("wallet")
            .username("wallet")
            .password("wallet")
            .codecRegistrar(EnumCodec.builder()
                .withEnum("network_type", NetworkType::class.java)
                .build())
            .build()
        return PostgresqlConnectionFactory(configuration)
    }

    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory?): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory!!)
    }

    @Bean
    fun r2dbcEntityTemplate(connectionFactory: ConnectionFactory?): R2dbcEntityTemplate {
        return R2dbcEntityTemplate(connectionFactory!!)
    }

}