package com.api.wallet.controller

import com.api.wallet.controller.dto.request.TransferRequest
import com.api.wallet.service.api.TransferService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/transfer")
class TransferController(
    private val transferService: TransferService,
) {

    @PostMapping
    fun transfer(
        @RequestBody request: TransferRequest,
    ): Mono<ResponseEntity<String>> {
        return transferService.transfer(request)
    }
}