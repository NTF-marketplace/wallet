package com.api.wallet.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class ReactiveProgramingService {

    fun blockingTest(i: Int) {
        println("glglt : ${i} ")
        Thread.sleep(5000)
    }

    fun nonBlockingTest(i: Int) : Mono<Void>{
        println("glglt : ${i} ")
        return Mono.delay(Duration.ofSeconds(5))
            .then(Mono.empty())
    }

}