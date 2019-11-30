package com.dietmap.yaak.api.appstore.subscription

import com.dietmap.yaak.api.appstore.receipt.ReceiptRequest
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
    fun handlePurchase(@RequestBody @Valid receiptRequest: ReceiptRequest): ResponseEntity<UserAppSubscriptionOrder?> {
        logger.debug("handlePurchase: $receiptRequest")

        val subscriptionOrder = subscriptionService.handlePurchase(receiptRequest)

        return ResponseEntity.ok(subscriptionOrder !!)
    }

    @PostMapping("/statusUpdateNotification")
    fun handleStatusUpdateNotification(@Valid @RequestBody statusUpdateNotification: StatusUpdateNotification): Any {
        logger.debug("handleStatusUpdateNotification $statusUpdateNotification")

        val subscriptionOrder = subscriptionService.handleSubscriptionNotification(statusUpdateNotification)

        logger.debug("handleStatusUpdateNotification $subscriptionOrder")

        return ResponseEntity.ok()
    }

}