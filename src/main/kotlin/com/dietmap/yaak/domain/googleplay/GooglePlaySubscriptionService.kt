package com.dietmap.yaak.domain.googleplay

import com.dietmap.yaak.domain.userapp.AppMarketplace
import com.dietmap.yaak.api.googleplay.GooglePlayNotificationType.*
import com.dietmap.yaak.api.googleplay.PubSubDeveloperNotification
import com.dietmap.yaak.domain.userapp.NotificationType
import com.dietmap.yaak.domain.userapp.UserAppClient
import com.dietmap.yaak.domain.userapp.UserAppSubscriptionNotification
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import com.google.api.services.androidpublisher.model.SubscriptionPurchasesAcknowledgeRequest
import com.google.common.base.Preconditions
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

    fun handlePurchase(packageName: String, subscriptionId: String, purchaseToken: String, userEmail: String?, initalBuy: Boolean = true): SubscriptionPurchase? {
        val subscriptionPurchase = androidPublisherApiClient.Purchases().Subscriptions().get(packageName, subscriptionId, purchaseToken).execute()
        //TODO translate into domain exceptions
        userEmail.apply {
            Preconditions.checkArgument(subscriptionPurchase.emailAddress == this, "Purchased subscription email does not match given email=$userEmail")
        }
        Preconditions.checkState(subscriptionPurchase.paymentState in listOf(PAYMENT_RECEIVED_CODE, PAYMENT_FREE_TRIAL_CODE), "Subscription has not been paid yet, paymentState=${subscriptionPurchase.paymentState}")
        try {
            val notificationResponse = userAppClient.sendSubscriptionNotification(UserAppSubscriptionNotification(
                    notificationType = if (initalBuy) NotificationType.INITIAL_BUY else NotificationType.RENEWAL,
                    appMarketplace = AppMarketplace.GOOGLE_PLAY,
                    countryCode = subscriptionPurchase.countryCode,
                    price = BigDecimal(subscriptionPurchase.priceAmountMicros).divide(BigDecimal(1000 * 1000)),
                    currencyCode = subscriptionPurchase.priceCurrencyCode,
                    transactionId = subscriptionPurchase.orderId,
                    productId = subscriptionId,
                    orderingUserInternalId = Integer.valueOf(subscriptionPurchase.developerPayload),
                    description = "Google Play initial subscription order",
                    expiryTimeMillis = subscriptionPurchase.expiryTimeMillis
            ))
            if (subscriptionPurchase.acknowledgementState == 0) {
                logger.info("Acknowledging Google Play subscription purchase of id=${subscriptionPurchase.orderId}, email=$userEmail")
                val content = SubscriptionPurchasesAcknowledgeRequest().setDeveloperPayload("applicationOrderId: ${notificationResponse?.orderId}")
                androidPublisherApiClient.Purchases().Subscriptions().acknowledge(packageName, subscriptionId, purchaseToken, content)
            }
            return subscriptionPurchase;
        } catch (e: WebClientResponseException) {
            //TODO make client return domain exceptions
            throw ResponseStatusException(e.statusCode, "UserAppSubscriptionNotification error", e)
        }
    }

    fun handleSubscriptionNotification(pubsubNotification: PubSubDeveloperNotification) {
        pubsubNotification.subscriptionNotification?.let {
            when(it.notificationType) {
                SUBSCRIPTION_PURCHASED -> handlePurchase(pubsubNotification.packageName, it.subscriptionId, it.purchaseToken, null)
                SUBSCRIPTION_RENEWED -> handlePurchase(pubsubNotification.packageName, it.subscriptionId, it.purchaseToken, null, false)
                SUBSCRIPTION_CANCELED -> logger.info("Subscription notification type: ${it.notificationType} is not handled")
                SUBSCRIPTION_RECOVERED -> logger.info("Subscription notification type: ${it.notificationType} is not handled")
                SUBSCRIPTION_ON_HOLD -> logger.info("Subscription notification type: ${it.notificationType} is not handled")
                SUBSCRIPTION_IN_GRACE_PERIOD -> logger.info("Subscription notification type: ${it.notificationType} is not handled\"")
                SUBSCRIPTION_RESTARTED -> logger.info("Subscription notification type: ${it.notificationType} is not handled\"")
                SUBSCRIPTION_PRICE_CHANGE_CONFIRMED -> logger.info("Subscription notification type: ${it.notificationType} is not handled\"")
                SUBSCRIPTION_DEFERRED -> logger.info("Subscription notification type: ${it.notificationType} is not handled\"")
                SUBSCRIPTION_PAUSED -> logger.info("Subscription notification type: ${it.notificationType} is not handled\"")
                SUBSCRIPTION_PAUSE_SCHEDULE_CHANGED -> logger.info("Subscription notification type: ${it.notificationType} is not handled\"")
                SUBSCRIPTION_REVOKED -> logger.info("Subscription notification type: ${it.notificationType} is not handled\"")
                SUBSCRIPTION_EXPIRED -> logger.info("Subscription notification type: ${it.notificationType} is not handled\"")
            }
        }
    }
}