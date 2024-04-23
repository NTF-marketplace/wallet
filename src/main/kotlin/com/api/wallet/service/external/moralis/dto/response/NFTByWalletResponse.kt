package com.api.wallet.service.external.moralis.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class NFTByWalletResponse(
    val status: String,
    val page: Int,
    @JsonProperty("page_size") val pageSize: Int,
    val cursor: String?,
    val result: List<NFTResult>
)

data class NFTResult(
    @JsonProperty("token_address") val tokenAddress: String,
    @JsonProperty("token_id") val tokenId: String,
    @JsonProperty("contract_type") val contractType: String,
    @JsonProperty("owner_of") val ownerOf: String,
    @JsonProperty("block_number") val blockNumber: String,
    @JsonProperty("block_number_minted") val blockNumberMinted: String?,
    @JsonProperty("token_uri") val tokenUri: String?,
    val metadata: String?,
    @JsonProperty("normalized_metadata") val normalizedMetadata: String?,
    val media: String?,
    val amount: String?,
    val name: String?,
    val symbol: String?,
    @JsonProperty("token_hash") val tokenHash: String?,
    @JsonProperty("last_token_uri_sync") val lastTokenUriSync: String?,
    @JsonProperty("last_metadata_sync") val lastMetadataSync: String?,
    @JsonProperty("possible_spam") val possibleSpam: Boolean?,
    @JsonProperty("verified_collection") val verifiedCollection: Boolean?,
    @JsonProperty("collection_logo") val collectionLogo: String?,
    @JsonProperty("collection_banner_image") val collectionBannerImage: String?,
    @JsonProperty("minter_address") val minterAddress: String?
)

data class NftMetadata(
    val name: String,
    val description: String,
    val image: String,
    @JsonProperty("animation_url") val animationUrl: String?,
    val attributes: List<Map<String, String>>,
    @JsonIgnoreProperties(ignoreUnknown = true) @JsonProperty("external_url") val externalUrl: String? = null
){
    companion object {
        fun toMetadataResponse(metadata: String): NftMetadata {
            val mapper = jacksonObjectMapper()
            return mapper.readValue(metadata, NftMetadata::class.java)
        }

        fun parseImage(image: String) : String {
            return image.replace("ipfs://", "https://ipfs.io/ipfs/")
        }
    }
}

data class NftAttribute(
    @JsonProperty("trait_type") val traitType: String?,
    val value: String?
) {
    companion object {
        fun toAttributeResponse(attributes: List<Map<String, String>>): List<NftAttribute> {
            return attributes.map {
                NftAttribute(
                    traitType = it["trait_type"],
                    value = it["value"]
                )
            }
        }
    }
}
