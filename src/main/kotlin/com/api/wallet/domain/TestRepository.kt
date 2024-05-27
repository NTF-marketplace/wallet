package com.api.wallet.domain

import com.api.wallet.enums.MyEnum
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface TestRepository : ReactiveCrudRepository<Test,Long> {

    fun findByType(enumType: MyEnum): Mono<Test>
}