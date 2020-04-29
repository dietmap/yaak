package com.dietmap.yaak.api.appstore.subscription

import com.dietmap.yaak.domain.appstore.AppStoreSubscriptionService
import com.dietmap.yaak.domain.checkArgument
import com.dietmap.yaak.domain.userapp.UserAppSubscriptionOrder
import mu.KotlinLogging
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
@RequestMapping("/api/appstore/subscriptions")
class SubscriptionController(private val subscriptionService: AppStoreSubscriptionService) {

    private val logger = KotlinLogging.logger { }

    @PostMapping("/purchase")
    fun handleInitialPurchase(@RequestBody @Valid subscriptionPurchaseRequest: SubscriptionPurchaseRequest): ResponseEntity<UserAppSubscriptionOrder?> {
        logger.debug { "handleInitialPurchase: $subscriptionPurchaseRequest" }

        val subscriptionOrder = subscriptionService.handleInitialPurchase(subscriptionPurchaseRequest)

        checkArgument(subscriptionOrder != null) { "Could not process SubscriptionPurchaseRequest $subscriptionPurchaseRequest in user app" }

        logger.debug { "handleInitialPurchase: $subscriptionOrder" }

        return ResponseEntity.ok(subscriptionOrder !!)
    }

    @PostMapping("/renew")
    fun handleAutoRenewal(@RequestBody @Valid subscriptionRenewRequest: SubscriptionRenewRequest): ResponseEntity<UserAppSubscriptionOrder?> {
        logger.debug { "handleAutoRenewal: $subscriptionRenewRequest" }

        val subscriptionOrder = subscriptionService.handleAutoRenewal(subscriptionRenewRequest)

        checkArgument(subscriptionOrder != null) { "Could not process SubscriptionRenewRequest $subscriptionRenewRequest in user app" }

        logger.debug { "handleAutoRenewal: $subscriptionOrder" }

        return ResponseEntity.ok(subscriptionOrder !!)
    }

    /**
     * Handler for Server 2 Server notifications
     */
    @PostMapping("/statusUpdateNotification")
    fun handleStatusUpdateNotification(@Valid @RequestBody statusUpdateNotification: StatusUpdateNotification): ResponseEntity<Any> {
        logger.debug { "handleStatusUpdateNotification: $statusUpdateNotification" }

        try {
            val subscriptionOrder = subscriptionService.handleSubscriptionNotification(statusUpdateNotification)

            checkArgument(subscriptionOrder != null) { "Could not process StatusUpdateNotification ${statusUpdateNotification.notificationType} in user app" }

            logger.debug { "handleStatusUpdateNotification: $subscriptionOrder" }
        } catch (ex : Exception) {

            // Send HTTP 50x or 40x to have the App Store retry the notification
            logger.error(ex) { "There was an error during handling server-2-server notification" }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }

        return ResponseEntity.ok().build()
    }

}