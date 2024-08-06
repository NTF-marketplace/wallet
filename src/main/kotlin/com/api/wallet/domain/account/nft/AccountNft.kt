package com.api.wallet.domain.account.nft

import com.api.wallet.enums.StatusType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

// 리스팅인지, 경매인지 상태값 나타내야됨
@Table("account_nft")
data class AccountNft(
    @Id val id: Long? = null,
    val accountId: Long,
    var nftId: Long,
    var status: StatusType = StatusType.NONE

) {
    fun update(status: StatusType): AccountNft {
        return this.copy(
            status = status
        )
    }
}

