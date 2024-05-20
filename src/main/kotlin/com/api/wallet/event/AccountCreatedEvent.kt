package com.api.wallet.event

import com.api.wallet.domain.account.Account


data class AccountCreatedEvent(val account: Account, val accountType: String, val timestamp:Long)
