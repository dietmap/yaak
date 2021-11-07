package com.dietmap.yaak.api.appstore.receipt

import com.dietmap.yaak.api.config.ApiCommons.TENANT_HEADER
import com.dietmap.yaak.domain.appstore.AppStoreSubscriptionService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@ConditionalOnProperty("yaak.app-store.enabled", havingValue = "true")
@RestController
@RequestMapping("/api/appstore/receipts")
class ReceiptController(private val subscriptionService: AppStoreSubscriptionService) {

    companion object {
        private const val VALID = "VALID"
        private const val NOT_VALID = "NOT_VALID"
    }

    @PostMapping
    fun verify(
        @RequestBody @Valid receiptRequest: ReceiptRequest,
        @RequestHeader(TENANT_HEADER, required = false) tenant: String?
    ): ResponseEntity<String> =
        if (subscriptionService.verifyReceipt(tenant, receiptRequest).isValid()) ResponseEntity.ok(VALID) else ResponseEntity.ok(NOT_VALID)

    @PostMapping("/verify")
    fun verifyWithResponse(
        @RequestBody @Valid receiptRequest: ReceiptRequest,
        @RequestHeader(TENANT_HEADER, required = false) tenant: String?
    ): ResponseEntity<ReceiptValidationResponse> =
        subscriptionService.verifyReceipt(tenant, receiptRequest)
            .let {
                if (it.isValid()) ResponseEntity.ok(ReceiptValidationResponse(it, VALID))
                else ResponseEntity.ok(ReceiptValidationResponse(it, NOT_VALID))
            }

}