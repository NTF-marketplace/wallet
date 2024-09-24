package com.api.wallet.domain.account.nft

import com.api.jooq.Tables.ACCOUNT
import com.api.jooq.Tables.ACCOUNT_NFT
import com.api.jooq.Tables.WALLET
import com.api.wallet.enums.ChainType
import com.api.wallet.enums.StatusType
import org.jooq.DSLContext
import org.jooq.conf.ParamType
import org.jooq.impl.DSL
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import reactor.core.publisher.Mono

class AccountNftRepositorySupportImpl(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
    private val dslContext: DSLContext
): AccountNftRepositorySupport {
    override fun findByNftIdAndWalletAddressAndChainType(
        nftId: Long,
        address: String,
        nftChainType: ChainType,
    ): Mono<AccountNft> {
        val query = dslContext
            .select(ACCOUNT_NFT.ID, ACCOUNT_NFT.ACCOUNT_ID, ACCOUNT_NFT.NFT_ID, ACCOUNT_NFT.STATUS)
            .from(ACCOUNT_NFT)
            .join(ACCOUNT).on(ACCOUNT_NFT.ACCOUNT_ID.eq(ACCOUNT.ID.cast(Long::class.java)))
            .join(WALLET).on(ACCOUNT.WALLET_ID.eq(WALLET.ID.cast(Long::class.java)))
            .where(WALLET.ADDRESS.eq(DSL.param("address", String::class.java)))
                .and(WALLET.CHAIN_TYPE.eq(DSL.param("chainType", com.api.jooq.enums.ChainType::class.java)))
                .and(ACCOUNT_NFT.NFT_ID.eq(DSL.param("nftId", Long::class.java)))

        val sql = query.getSQL(ParamType.NAMED)

        return r2dbcEntityTemplate.databaseClient.sql(sql)
            .bind("address", address)
            .bind("chainType", nftChainType)
            .bind("nftId", nftId)
            .map { row, _ ->
                AccountNft(
                    id = (row.get("id") as Number).toLong(),
                    accountId = (row.get("account_id") as Number).toLong(),
                    nftId = (row.get("nft_id") as Number).toLong(),
                    status = StatusType.valueOf(row.get("status", String::class.java)!!)
                )
            }
            .one()
    }
}