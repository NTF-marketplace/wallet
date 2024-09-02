package com.api.wallet.event

import com.api.wallet.domain.account.Account
import com.api.wallet.domain.account.nft.AccountNft
import com.api.wallet.enums.AccountType
import com.api.wallet.enums.TransferType
import java.math.BigDecimal


data class AccountEvent(val account: Account, val accountType: AccountType, val balance: BigDecimal)

data class AccountNftEvent(val accountNft: AccountNft,val accountType: AccountType)
