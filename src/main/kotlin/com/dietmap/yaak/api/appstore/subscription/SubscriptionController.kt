package com.dietmap.yaak.api.appstore.subscription

import com.dietmap.yaak.domain.appstore.AppStoreSubscriptionService
import com.dietmap.yaak.domain.userapp.UserAppSubscriptionOrder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
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

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/purchase")
    fun handleInitialPurchase(@RequestBody @Valid subscriptionPurchaseRequest: SubscriptionPurchaseRequest): ResponseEntity<UserAppSubscriptionOrder?> {
        logger.debug("handleInitialPurchase: $subscriptionPurchaseRequest")

        val subscriptionOrder = subscriptionService.handleInitialPurchase(subscriptionPurchaseRequest)

        logger.debug("handleInitialPurchase: $subscriptionOrder")

        return ResponseEntity.ok(subscriptionOrder !!)
    }

    @PostMapping("/renew")
    fun handleAutoRenewal(@RequestBody @Valid subscriptionRenewRequest: SubscriptionRenewRequest): ResponseEntity<UserAppSubscriptionOrder?> {
        logger.debug("handleAutoRenewal: $subscriptionRenewRequest")

        val subscriptionOrder = subscriptionService.handleAutoRenewal(subscriptionRenewRequest)

        logger.debug("handleAutoRenewal: $subscriptionOrder")

        return ResponseEntity.ok(subscriptionOrder !!)
    }

    /**
     * Handler for Server to server notifications
     */
    @PostMapping("/statusUpdateNotification")
    fun handleStatusUpdateNotification(@Valid @RequestBody statusUpdateNotification: StatusUpdateNotification): ResponseEntity<Any> {
        logger.debug("handleStatusUpdateNotification: $statusUpdateNotification")

        val subscriptionOrder = subscriptionService.handleSubscriptionNotification(statusUpdateNotification)

        logger.debug("handleStatusUpdateNotification: $subscriptionOrder")

        // Send HTTP 50x or 40x to have the App Store retry the notification if the post was not successful.
        return ResponseEntity.ok().build()
    }

}