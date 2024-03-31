package com.api.wallet.domain.walletNft.repository

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import reactor.core.publisher.Flux

class WalletNftRepositorySupportImpl(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
): WalletNftRepositorySupport {


    override fun findByWalletIdJoinNft(address: String, networkType: String): Flux<WalletNftWithNft> {
        val query = """
        SELECT 
            wn.id AS wn_id, 
            wn.wallet_id AS wallet_address, 
            n.id AS nft_id, 
            n.token_address AS nft_token_address, 
            n.token_id AS nft_token_id 
        FROM wallet_nft wn 
        JOIN wallet w ON wn.wallet_id = w.id 
        JOIN nft n ON wn.nft_id = n.id 
        WHERE w.address = $1 AND w.network_type = $2
    """

        return r2dbcEntityTemplate.databaseClient.sql(query)
            .bind(0, address)
            .bind(1, networkType)
            .map { row, metadata ->
                WalletNftWithNft(
                    id = (row.get("wn_id") as Number).toLong(),
                    walletId = (row.get("wallet_id") as Number).toLong(),
                    nftId = (row.get("nft_id") as Number).toLong(),
                    nftTokenAddress = row.get("nft_token_address", String::class.java)!!,
                    nftTokenId = row.get("nft_token_id", String::class.java)!!
                )
            }
            .all()
    }

}