package com.dietmap.yaak.domain.appstore

import com.dietmap.yaak.api.appstore.receipt.ReceiptRequest
import com.dietmap.yaak.api.appstore.subscription.NotificationStatus
import com.dietmap.yaak.api.appstore.subscription.StatusUpdateNotification
import com.dietmap.yaak.domain.userapp.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AppStoreSubscriptionService(val userAppClient: UserAppClient, val appStoreClient: AppStoreClient) {

    private val logger: Logger = LoggerFactory.getLogger(AppStoreSubscriptionService::class.java)

    fun handlePurchase(receiptRequest: ReceiptRequest) : UserAppSubscriptionOrder? {

        val receiptResponse = appStoreClient.verifyReceipt(receiptRequest)

        val notification = UserAppSubscriptionNotification(
                // TODO match all fields in here
                notificationType = NotificationType.SUBSCRIPTION_PURCHASED,
                productId = receiptResponse.receipt.bundleId,
                transactionId = "transactionId",
                price = BigDecimal.ONE,
                countryCode = "PL",
                currencyCode = "PLN",
                appMarketplace = AppMarketplace.APP_STORE,
                description = "Subscription purchase from AppStore",
                expiryTimeMillis = receiptResponse.receipt.expirationDateMs.toLong()
        )

        return userAppClient.sendSubscriptionNotification(notification)
    }

    fun handleSubscriptionNotification(statusUpdateNotification: StatusUpdateNotification) : UserAppSubscriptionOrder? {

        logger.debug("Processing notification: ${statusUpdateNotification.notificationType}")

        when (statusUpdateNotification.notificationType) {
            NotificationStatus.INITIAL_BUY -> {}
            NotificationStatus.CANCEL -> {}
            NotificationStatus.RENEWAL -> {}
            NotificationStatus.INTERACTIVE_RENEWAL -> {}
            NotificationStatus.DID_CHANGE_RENEWAL_PREF -> {}
            NotificationStatus.DID_CHANGE_RENEWAL_STATUS -> {}
        }

        // verify the latest receipt against the iTunes server
        val receiptResponse = appStoreClient.verifyReceipt(
                ReceiptRequest(statusUpdateNotification.latestReceipt, "passsword", true))

        val notification = UserAppSubscriptionNotification(
                // TODO match all fields in here
                notificationType = NotificationType.SUBSCRIPTION_RENEWED,
                productId = receiptResponse.receipt.bundleId,
                transactionId = "transactionId",
                price = BigDecimal.ONE,
                countryCode = "PL",
                currencyCode = "PLN",
                appMarketplace = AppMarketplace.APP_STORE,
                description = "Subscription update from AppStore",
                expiryTimeMillis = receiptResponse.receipt.expirationDateMs.toLong()
        )

        return userAppClient.sendSubscriptionNotification(notification)
    }

    fun isReceiptVerified(receiptRequest: ReceiptRequest) : Boolean {
        return appStoreClient.isVerified(receiptRequest)
    }
}