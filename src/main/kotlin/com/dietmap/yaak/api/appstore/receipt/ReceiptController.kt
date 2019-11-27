package com.dietmap.yaak.api.appstore.receipt

import com.dietmap.yaak.domain.appstore.AppStoreClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid


@ConditionalOnProperty("yaak.app-store.enabled", havingValue = "true")
@RestController
@RequestMapping("/api/appstore/receipts")
class ReceiptController(private val appStoreClient : AppStoreClient) {

    @PostMapping("/verify")
    fun verifyWithResponse(@RequestBody @Valid receiptRequest: ReceiptRequest) : ResponseEntity<ReceiptResponse> {
        val receiptResponse = appStoreClient.verifyReceipt(receiptRequest)

        return if (receiptResponse.isValid()) ResponseEntity.ok(receiptResponse) else ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(receiptResponse)
    }
}