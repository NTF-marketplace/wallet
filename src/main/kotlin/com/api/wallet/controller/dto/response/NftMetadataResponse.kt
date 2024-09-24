package com.api.wallet.controller.dto.response

import com.api.wallet.enums.ChainType
import com.api.wallet.enums.ContractType
import com.api.wallet.enums.TokenType
import java.math.BigDecimal

data class NftMetadataResponse(
    val id: Long,
    val tokenId: String,
    val tokenAddress: String,
    val contractType: ContractType,
    val chainType: ChainType,
    val nftName: String,
    val collectionName: String,
    val image: String,
    val lastPrice: BigDecimal?,
    val collectionLogo: String?,
)
