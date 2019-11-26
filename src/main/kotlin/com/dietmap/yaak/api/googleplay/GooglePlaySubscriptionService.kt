package com.dietmap.yaak.api.googleplay

import com.dietmap.yaak.api.googleplay.GooglePlayNotificationType.*
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
        private val PAYMENT_RECEIVED_CODE = 1
        private val PAYMENT_FREE_TRIAL_CODE = 2
        private val logger: Logger = LoggerFactory.getLogger(GooglePlaySubscriptionService::class.java)
    }

    fun handlePurchase(packageName: String, subscriptionId: String, purchaseToken: String, userEmail: String?): SubscriptionPurchase? {
        val subscriptionPurchase = androidPublisherApiClient.Purchases().Subscriptions().get(packageName, subscriptionId, purchaseToken).execute()
        //TODO translate into domain exceptions
        userEmail.apply {
            Preconditions.checkArgument(subscriptionPurchase.emailAddress == this, "Purchased subscription email does not match given email=$userEmail")
        }
        Preconditions.checkState(subscriptionPurchase.paymentState in listOf(PAYMENT_RECEIVED_CODE, PAYMENT_FREE_TRIAL_CODE), "Subscription has not been paid yet, paymentState=${subscriptionPurchase.paymentState}")
        try {
            val notificationResponse = userAppClient.sendSubscriptionNotification(UserAppSubscriptionNotification(
                    notificationType = NotificationType.INITIAL_BUY,
                    appMarketplace = "Google Play",
                    countryCode = subscriptionPurchase.countryCode,
                    price = BigDecimal(subscriptionPurchase.priceAmountMicros).divide(BigDecimal(1000 * 1000)),
                    currencyCode = subscriptionPurchase.priceCurrencyCode,
                    transactionId = subscriptionPurchase.orderId,
                    productId = subscriptionPurchase.kind,
                    orderingUserInternalId = Integer.valueOf(subscriptionPurchase.developerPayload),
                    description = "Google Play initial subscription order"
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
                SUBSCRIPTION_RECOVERED -> TODO()
                SUBSCRIPTION_RENEWED -> TODO()
                SUBSCRIPTION_CANCELED -> TODO()
                SUBSCRIPTION_ON_HOLD -> TODO()
                SUBSCRIPTION_IN_GRACE_PERIOD -> TODO()
                SUBSCRIPTION_RESTARTED -> TODO()
                SUBSCRIPTION_PRICE_CHANGE_CONFIRMED -> TODO()
                SUBSCRIPTION_DEFERRED -> TODO()
                SUBSCRIPTION_PAUSED -> TODO()
                SUBSCRIPTION_PAUSE_SCHEDULE_CHANGED -> TODO()
                SUBSCRIPTION_REVOKED -> TODO()
                SUBSCRIPTION_EXPIRED -> TODO()
            }
        }
    }
}