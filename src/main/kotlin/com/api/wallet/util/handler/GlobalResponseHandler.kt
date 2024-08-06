package com.api.wallet.util.handler

import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import reactor.core.publisher.Mono

// TODO(이걸 api 게이트웨이에서 처리하면 어떨까?)
//@ControllerAdvice(annotations = [RestController::class])
class GlobalResponseHandler: ResponseBodyAdvice<Any> {
    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        return true
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: org.springframework.http.server.ServerHttpRequest,
        response: org.springframework.http.server.ServerHttpResponse
    ): Any? {
        return if (body is Mono<*>) {
            body.map {
                if (it is ResponseEntity<*>) {
                    val responseBody = (it.body as? ResponseWrapper<*>) ?: ResponseWrapper(it.body)
                    ResponseEntity(responseBody, it.statusCode)
                } else {
                    ResponseEntity(ResponseWrapper(it), HttpStatus.OK)
                }
            }.onErrorResume {
                Mono.just(ResponseEntity(ResponseWrapper(null, it.message), HttpStatus.INTERNAL_SERVER_ERROR))
            }
        } else {
            ResponseEntity(ResponseWrapper(body), HttpStatus.OK)
        }
    }
}