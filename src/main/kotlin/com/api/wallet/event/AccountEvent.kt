package com.api.wallet.event

import com.api.wallet.domain.account.Account
import com.api.wallet.domain.account.nft.AccountNft


data class AccountEvent(val account: Account, val accountType: String, val timestamp:Long)

data class AccountNftEvent(val accountNft: AccountNft,val accountType: String, val timestamp: Long)
