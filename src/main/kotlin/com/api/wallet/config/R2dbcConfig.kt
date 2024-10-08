package com.api.wallet.config

import com.api.wallet.enums.*
import com.api.wallet.util.enumConvert.*
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.codec.EnumCodec
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import java.util.*


@Configuration
@EnableR2dbcRepositories
class R2dbcConfig : AbstractR2dbcConfiguration(){

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        val config = PostgresqlConnectionConfiguration.builder()
            .host("localhost")
            .port(5432)
            .database("wallet")
            .username("wallet")
            .password("wallet")
            .codecRegistrar(
                EnumCodec.builder()
                    .withEnum("my_enum", MyEnum::class.java)
                    .withEnum("chain_type", ChainType::class.java)
                    .withEnum("account_type", AccountType::class.java)
                    .withEnum("transfer_type", TransferType::class.java)
                    .withEnum("status_type", StatusType::class.java)
                    .withEnum("transaction_status_type", TransaionStatusType::class.java)
                    .build()
            )
            .build()

        val poolConfig = ConnectionPoolConfiguration.builder()
            .connectionFactory(PostgresqlConnectionFactory(config))
            .maxSize(10)
            .build()

        return ConnectionPool(poolConfig)
    }
    @Bean
    override fun r2dbcCustomConversions(): R2dbcCustomConversions {
        val converters: MutableList<Converter<*, *>?> = ArrayList<Converter<*, *>?>()
        converters.add(MyEnumConverter(MyEnum::class.java))
        converters.add(StringToEnumConverter(MyEnum::class.java))
        converters.add(ChinTypeConvert(ChainType::class.java))
        converters.add((StringToEnumConverter(ChainType::class.java)))
        converters.add(AccountTypeConvert(AccountType::class.java))
        converters.add((StringToEnumConverter(AccountType::class.java)))
        converters.add(TransferTypeConvert(TransferType::class.java))
        converters.add((StringToEnumConverter(TransferType::class.java)))
        converters.add(StatusTypeConvert(StatusType::class.java))
        converters.add((StringToEnumConverter(StatusType::class.java)))
        converters.add(TransactionStatusTypeConvert(TransaionStatusType::class.java))
        converters.add((StringToEnumConverter(TransaionStatusType::class.java)))
        return R2dbcCustomConversions(storeConversions, converters)
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