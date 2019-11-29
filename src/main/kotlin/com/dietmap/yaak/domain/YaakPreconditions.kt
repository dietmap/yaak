package com.dietmap.yaak.domain

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

inline fun checkArgument(value: Boolean, lazyMessage: () -> Any) {
    if (!value) {
        val message = lazyMessage()
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, message.toString())
    }
}