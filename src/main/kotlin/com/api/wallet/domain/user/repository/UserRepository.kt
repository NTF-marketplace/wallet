package com.api.wallet.domain.user.repository

import com.api.wallet.domain.user.Users
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface UserRepository : ReactiveCrudRepository<Users,Long> {
}