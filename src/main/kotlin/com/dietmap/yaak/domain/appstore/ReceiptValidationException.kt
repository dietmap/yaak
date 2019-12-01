package com.dietmap.yaak.domain.appstore

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class ReceiptValidationException(message : String) : RuntimeException() {
}