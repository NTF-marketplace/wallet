package com.api.wallet.domain.account.nft

import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface AccountNftRepository: ReactiveCrudRepository<AccountNft,Long> {
}