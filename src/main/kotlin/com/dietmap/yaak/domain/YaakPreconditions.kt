package com.dietmap.yaak.domain

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

inline fun checkArgument(value: Boolean, lazyMessage: () -> Any) {
    if (!value) {
        val message = lazyMessage()
        throw ArgumentNotValidException(message.toString())
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class ArgumentNotValidException(message: String) : RuntimeException(message)
