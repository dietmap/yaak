package com.dietmap.yaak.api.googleplay

import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@ConditionalOnBean(GooglePlaySubscriptionService::class)
@RestController
@RequestMapping("/api/googleplay/purchase")
class PurchaseController(val subscriptionService: GooglePlaySubscriptionService) {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping
    fun purchase(@RequestBody @Valid purchaseRequest: PurchaseRequest): SubscriptionPurchase? {
        logger.info("Received purchase request from Google Play: {}", purchaseRequest)
        return subscriptionService.handlePurchase(purchaseRequest.packageName, purchaseRequest.subscriptionId, purchaseRequest.purchaseToken, purchaseRequest.userEmail)
    }
}

data class PurchaseRequest(
        @NotBlank
        val packageName: String,
        @NotBlank
        val subscriptionId: String,
        @NotBlank
        val purchaseToken: String,
        @NotBlank @Email
        val userEmail: String
)