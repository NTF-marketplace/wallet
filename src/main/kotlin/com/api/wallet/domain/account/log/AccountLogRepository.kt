package com.api.wallet.domain.account.log

import org.springframework.data.repository.reactive.ReactiveCrudRepository


interface AccountLogRepository : ReactiveCrudRepository<AccountLog,Long> {
}