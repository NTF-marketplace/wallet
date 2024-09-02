package com.api.wallet.domain.account.nft

import com.api.wallet.enums.ChainType
import com.api.wallet.enums.StatusType
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import reactor.core.publisher.Mono

class AccountNftRepositorySupportImpl(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
): AccountNftRepositorySupport {
    override fun findByNftIdAndWalletAddressAndChainType(
        nftId: Long,
        address: String,
        nftChainType: ChainType,
    ): Mono<AccountNft> {
        val query = """
            SELECT an.*
            FROM account_nft an
            JOIN account a ON an.account_id = a.id
            JOIN wallet w ON a.wallet_id = w.id
            WHERE an.nft_id = $1 AND w.address = $2 AND w.chain_type = $3
        """

        return r2dbcEntityTemplate.databaseClient
            .sql(query)
            .bind(0, nftId)
            .bind(1, address)
            .bind(2, nftChainType)
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