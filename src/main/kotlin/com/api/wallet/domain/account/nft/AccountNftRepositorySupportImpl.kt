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
        address: String
    ): Mono<AccountNft> {
        val query = """
            SELECT an.*
            FROM account_nft an
            JOIN nft n ON an.nft_id = n.id
            JOIN account a ON an.account_id = a.id
            JOIN wallet w ON a.wallet_id = w.id
            WHERE n.id = $1 AND w.address = $2 AND n.chain_type = (
                SELECT chain_type FROM nft WHERE id = $1
            )
        """

        return r2dbcEntityTemplate.databaseClient
            .sql(query)
            .bind(0, nftId)
            .bind(1, address)
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