package com.api.wallet.domain.walletNft.repository

import com.api.wallet.enums.ChainType
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import reactor.core.publisher.Flux

class WalletNftRepositorySupportImpl(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
): WalletNftRepositorySupport {


    override fun findByWalletIdJoinNft(address: String, chainType: ChainType): Flux<WalletNftDto> {
        val query = """
        SELECT 
            wn.id AS wn_id, 
            wn.wallet_id AS wallet_address, 
            wn.nft_id AS nft_id
        FROM wallet_nft wn 
        JOIN wallet w ON wn.wallet_id = w.id  
        WHERE w.address = $1 AND w.chain_type = $2
    """

        return r2dbcEntityTemplate.databaseClient.sql(query)
            .bind(0, address)
            .bind(1, chainType)
            .map { row, data ->
                WalletNftDto(
                    id = (row.get("wn_id") as Number).toLong(),
                    walletId = (row.get("wallet_address") as Number).toLong(),
                    nftId = (row.get("nft_id") as Number).toLong(),
                )
            }
            .all()
    }

}