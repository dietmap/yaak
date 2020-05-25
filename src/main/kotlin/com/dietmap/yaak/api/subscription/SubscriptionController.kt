package com.dietmap.yaak.api.subscription

import com.dietmap.yaak.domain.googleplay.GooglePlaySubscriptionService
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@ConditionalOnProperty("yaak.google-play.enabled", havingValue = "true")
@RestController
class SubscriptionController(val subscriptionService: GooglePlaySubscriptionService) {

    private val logger = KotlinLogging.logger { }

    @PostMapping("/subscriptions/cancel")
    fun cancelSubscriptionPurchase(@RequestBody @Valid cancelRequest: SubscriptionCancelRequest) {
        logger.info { "Received subscription purchase cancellation request: $cancelRequest" }
        subscriptionService.cancelPurchase(cancelRequest)
    }

}

data class SubscriptionCancelRequest(
        @NotBlank val packageName: String,
        @NotBlank val subscriptionId: String,
        @NotBlank val purchaseToken: String
)