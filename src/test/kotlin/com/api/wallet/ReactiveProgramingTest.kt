package com.api.wallet

import com.api.wallet.service.ReactiveProgramingService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux

@SpringBootTest
class ReactiveProgramingTest(
    @Autowired private val reactiveProgramingService: ReactiveProgramingService,
) {

    @Test
    fun test() {
        Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9)
            .filter { it % 2 == 0 }
            .map { it * 2 }
            .subscribe { println(it) }

    }


    @Test
    fun blocking() {
        val start = System.currentTimeMillis()
        for(i: Int in 1..5){
            reactiveProgramingService.blockingTest(i)
        }
        val end = System.currentTimeMillis()
        println("result : " + end.minus(start))
    }

    @Test
    fun nonBlocking() {
        val start = System.currentTimeMillis()
        Flux.range(1,5)
            .flatMap { reactiveProgramingService.nonBlockingTest(it) }.then().block()

        val end = System.currentTimeMillis()
        println("result : " + (end - start))
    }



}