package com.api.wallet.domain.walletNft.repository

import com.api.jooq.tables.Wallet.WALLET
import com.api.jooq.tables.WalletNft.WALLET_NFT
import com.api.wallet.enums.ChainType
import org.jooq.DSLContext
import org.jooq.conf.ParamType
import org.jooq.impl.DSL
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import reactor.core.publisher.Flux

class WalletNftRepositorySupportImpl(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
    private val dslContext: DSLContext
): WalletNftRepositorySupport {

    override fun findByWalletIdJoinNft(address: String, chainType: ChainType): Flux<WalletNftDto> {
        val query = dslContext
            .select(
                WALLET_NFT.ID.`as`("wn_id"),
                WALLET.ID.`as`("wallet_address"),
                WALLET_NFT.NFT_ID.`as`("nft_id")
            )
            .from(WALLET_NFT)
            .join(WALLET).on(WALLET_NFT.WALLET_ID.eq(WALLET.ID.cast(Long::class.java)))
            .where(WALLET.ADDRESS.eq(DSL.param("address", String::class.java)))
            .and(WALLET.CHAIN_TYPE.eq(DSL.param("chainType", com.api.jooq.enums.ChainType::class.java)))

        val sql = query.getSQL(ParamType.NAMED)

        return r2dbcEntityTemplate.databaseClient.sql(sql)
            .bind("address", address)
            .bind("chainType", chainType)
            .map { row, _ -> WalletNftDto.fromRow(row) }
            .all()
    }

}