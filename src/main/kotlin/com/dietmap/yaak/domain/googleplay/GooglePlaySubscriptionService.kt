package com.dietmap.yaak.domain.googleplay

import com.dietmap.yaak.api.googleplay.GooglePlayNotificationType.SUBSCRIPTION_PURCHASED
import com.dietmap.yaak.api.googleplay.GooglePlayNotificationType.SUBSCRIPTION_RENEWED
import com.dietmap.yaak.api.googleplay.GooglePlaySubscriptionNotification
import com.dietmap.yaak.api.googleplay.PubSubDeveloperNotification
import com.dietmap.yaak.domain.userapp.AppMarketplace
import com.dietmap.yaak.domain.userapp.NotificationType
import com.dietmap.yaak.domain.userapp.UserAppClient
import com.dietmap.yaak.domain.userapp.UserAppSubscriptionNotification
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import com.google.api.services.androidpublisher.model.SubscriptionPurchasesAcknowledgeRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal


@ConditionalOnBean(AndroidPublisher::class)
@Service
class GooglePlaySubscriptionService(val androidPublisherApiClient: AndroidPublisher, val userAppClient: UserAppClient) {
    companion object {
        private const val PAYMENT_RECEIVED_CODE = 1
        private const val PAYMENT_FREE_TRIAL_CODE = 2
        private val logger: Logger = LoggerFactory.getLogger(GooglePlaySubscriptionService::class.java)
    }

    fun handlePurchase(packageName: String, subscriptionId: String, purchaseToken: String, initalBuy: Boolean = true): SubscriptionPurchase? {
        val subscription = androidPublisherApiClient.Purchases().Subscriptions().get(packageName, subscriptionId, purchaseToken).execute()
//        checkArgument(subscription.paymentState in listOf(PAYMENT_RECEIVED_CODE, PAYMENT_FREE_TRIAL_CODE)) { "Subscription has not been paid yet, paymentState=${subscription.paymentState}" }
        try {
            val notificationResponse = userAppClient.sendSubscriptionNotification(UserAppSubscriptionNotification(
                    notificationType = if (initalBuy) NotificationType.SUBSCRIPTION_PURCHASED else NotificationType.SUBSCRIPTION_RENEWED,
                    appMarketplace = AppMarketplace.GOOGLE_PLAY,
                    countryCode = subscription.countryCode,
                    price = BigDecimal(subscription.priceAmountMicros).divide(BigDecimal(1000 * 1000)),
                    currencyCode = subscription.priceCurrencyCode,
                    transactionId = subscription.orderId,
                    productId = subscriptionId,
                    description = "Google Play ${if (initalBuy) "initial" else "renewal"} subscription order",
                    expiryTimeMillis = subscription.expiryTimeMillis
            ))
            if (subscription.acknowledgementState == 0) {
                logger.info("Acknowledging Google Play subscription purchase of id=${subscription.orderId}, purchaseToken=$purchaseToken")
                val content = SubscriptionPurchasesAcknowledgeRequest().setDeveloperPayload("applicationOrderId: ${notificationResponse?.orderId}")
                androidPublisherApiClient.Purchases().Subscriptions().acknowledge(packageName, subscriptionId, purchaseToken, content)
            }
            return subscription;
        } catch (e: WebClientResponseException) {
            throw ResponseStatusException(e.statusCode, "Error communicating with user app", e)
        }
    }

    fun handleSubscriptionNotification(pubsubNotification: PubSubDeveloperNotification) {
        pubsubNotification.subscriptionNotification?.let {
            when (it.notificationType) {
                SUBSCRIPTION_PURCHASED -> handlePurchase(pubsubNotification.packageName, it.subscriptionId, it.purchaseToken)
                SUBSCRIPTION_RENEWED -> handlePurchase(pubsubNotification.packageName, it.subscriptionId, it.purchaseToken, false)
                else -> handleStatusUpdate(pubsubNotification.packageName, it)
            }
        }
    }

    private fun handleStatusUpdate(packageName: String, notification: GooglePlaySubscriptionNotification) {
        val subscription = androidPublisherApiClient.Purchases().Subscriptions().get(packageName, notification.subscriptionId, notification.purchaseToken).execute()
        try {
            val subscriptionUpdate = UserAppSubscriptionNotification(
                    notificationType = NotificationType.valueOf(notification.notificationType.name),
                    appMarketplace = AppMarketplace.GOOGLE_PLAY,
                    countryCode = subscription.countryCode,
                    price = BigDecimal(subscription.priceAmountMicros).divide(BigDecimal(1000 * 1000)),
                    currencyCode = subscription.priceCurrencyCode,
                    transactionId = subscription.orderId,
                    productId = notification.subscriptionId,
                    description = "Google Play subscription update: " + notification.notificationType,
                    expiryTimeMillis = subscription.expiryTimeMillis
            )
            userAppClient.sendSubscriptionNotification(subscriptionUpdate)
            logger.info("Google Play subscription notification has been passed to user app: $subscriptionUpdate")
        } catch (e: WebClientResponseException) {
            throw ResponseStatusException(e.statusCode, "Error communicating with user app", e)
        }
    }
}