package com.dietmap.yaak.domain.appstore

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
class ReceiptValidationException(message : String) : RuntimeException(message) {
}