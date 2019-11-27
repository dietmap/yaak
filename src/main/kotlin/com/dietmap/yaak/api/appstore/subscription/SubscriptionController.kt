package com.dietmap.yaak.api.appstore.subscription

import com.dietmap.yaak.api.appstore.receipt.ReceiptRequest
import com.dietmap.yaak.domain.appstore.AppStoreClient
import com.dietmap.yaak.domain.userapp.AppMarketplace
import com.dietmap.yaak.domain.userapp.NotificationType
import com.dietmap.yaak.domain.userapp.UserAppClient
import com.dietmap.yaak.domain.userapp.UserAppSubscriptionNotification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import javax.validation.Valid


@ConditionalOnProperty("yaak.app-store.enabled", havingValue = "true")
@RestController
@RequestMapping("/api/appstore/subscriptions")
class SubscriptionController(private val userAppClient: UserAppClient, private val appStoreClient: AppStoreClient) {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/purchase")
    fun purchase(@RequestBody @Valid receiptRequest: ReceiptRequest): ResponseEntity<String> {
        val receiptResponse = appStoreClient.verifyReceipt(receiptRequest)

        return if (appStoreClient.isVerified(receiptRequest)) {

            //TODO fill in with proper data and handle errors
            val notification = UserAppSubscriptionNotification(
                    notificationType = NotificationType.RENEWAL,
                    productId = "",
                    orderingUserInternalId = 1,
                    transactionId = "",
                    price = BigDecimal.ONE,
                    countryCode = "PL",
                    currencyCode = "PLN",
                    appMarketplace = AppMarketplace.APP_STORE,
                    description = "Recipe update from AppStore"
            )
            userAppClient.sendSubscriptionNotification(notification)

            return ResponseEntity.ok("Status Update Notification received")
        }

        else ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

    }

    @PostMapping("/statusUpdateNotification")
    fun statusUpdateNotification(@Valid @RequestBody statusUpdateNotification: StatusUpdateNotification): ResponseEntity<String> {

        logger.debug("StatusUpdateNotification {}", statusUpdateNotification)

        // TODO send subscription notification
        // userAppClient.sendSubscriptionNotification(statusUpdateNotification)

        return ResponseEntity.ok("Status Update Notification received")
    }

}