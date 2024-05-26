package com.api.wallet.event

import com.api.wallet.domain.account.Account
import com.api.wallet.domain.account.nft.AccountNft
import com.api.wallet.enums.AccountType
import com.api.wallet.enums.TransferType


data class AccountEvent(val account: Account, val accountType: AccountType, val timestamp:Long)

data class AccountNftEvent(val accountNft: AccountNft,val accountType: AccountType, val timestamp: Long)
