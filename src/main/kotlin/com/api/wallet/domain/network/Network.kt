package com.api.wallet.domain.network

import com.api.wallet.enums.NetworkType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("network")
data class Network(
    @Id val type: String
){

}