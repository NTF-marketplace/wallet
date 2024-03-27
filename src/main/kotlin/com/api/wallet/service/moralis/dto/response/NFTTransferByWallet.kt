package com.api.wallet.service.moralis.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class NFTTransferByWallet(
    val page: Int,
    @JsonProperty("page_size") val pageSize: Int,
    val cursor: String?,
    val result: List<TransferResult>,
    @JsonProperty("block_exists") val blockExists: Boolean
)


data class TransferResult(
    @JsonProperty("block_number") val blockNumber: String,
    @JsonProperty("block_timestamp") val blockTimestamp: String,
    @JsonProperty("block_hash") val blockHash: String,
    @JsonProperty("transaction_hash") val transactionHash: String,
    @JsonProperty("transaction_index") val transactionIndex: Int,
    @JsonProperty("log_index") val logIndex: Int,
    val value: String,
    @JsonProperty("contract_type") val contractType: String,
    @JsonProperty("transaction_type") val transactionType: String,
    @JsonProperty("token_address") val tokenAddress: String,
    @JsonProperty("token_id") val tokenId: String,
    @JsonProperty("from_address") val fromAddress: String,
    @JsonProperty("from_address_label") val fromAddressLabel: String?,
    @JsonProperty("to_address") val toAddress: String,
    @JsonProperty("to_address_label") val toAddressLabel: String?,
    val amount: String,
    val verified: Int,
    @JsonProperty("operator") val operator: String,
    @JsonProperty("possible_spam") val possibleSpam: Boolean,
    @JsonProperty("verified_collection") val verifiedCollection: Boolean
)