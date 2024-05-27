package com.api.wallet.domain.account.log

import com.api.wallet.enums.AccountType
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux


interface AccountLogRepository : ReactiveCrudRepository<AccountLog,Long> {

}