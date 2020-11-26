package com.dietmap.yaak.domain.googleplay

import com.dietmap.yaak.api.googleplay.GooglePlayNotificationType.SUBSCRIPTION_PURCHASED
import com.dietmap.yaak.api.googleplay.GooglePlayNotificationType.SUBSCRIPTION_RENEWED
import com.dietmap.yaak.api.googleplay.GooglePlaySubscriptionNotification
import com.dietmap.yaak.api.googleplay.PubSubDeveloperNotification
import com.dietmap.yaak.api.googleplay.PurchaseRequest
import com.dietmap.yaak.api.googleplay.SubscriptionCancelRequest
import com.dietmap.yaak.domain.checkArgument
import com.dietmap.yaak.domain.userapp.*
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import com.google.api.services.androidpublisher.model.SubscriptionPurchasesAcknowledgeRequest
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service
import java.math.BigDecimal


@ConditionalOnBean(AndroidPublisher::class)
@Service
class GooglePlaySubscriptionService(val androidPublisherApiClient: AndroidPublisher, val userAppClient: UserAppClient) {

    private val PAYMENT_RECEIVED_CODE = 1
    private val PAYMENT_FREE_TRIAL_CODE = 2
    private val USER_ACCOUNT_ID_KEY = "obfuscatedExternalAccountId"
    private val USER_APP_STATUS_ACTIVE = "ACTIVE"
    private val logger = KotlinLogging.logger { }

    fun handlePurchase(purchaseRequest: PurchaseRequest): SubscriptionPurchase? {
        val subscription = androidPublisherApiClient.purchases().subscriptions().get(purchaseRequest.packageName, purchaseRequest.subscriptionId, purchaseRequest.purchaseToken).execute()
        checkArgument(subscription.paymentState in listOf(PAYMENT_RECEIVED_CODE, PAYMENT_FREE_TRIAL_CODE)) { "Subscription has not been paid yet, paymentState=${subscription.paymentState}" }

        logger.info { "Handling purchase: $subscription, initial: ${subscription.isInitialPurchase()}" }

        val notificationResponse = userAppClient.sendSubscriptionNotification(UserAppSubscriptionNotification(
                notificationType = if (subscription.isInitialPurchase()) NotificationType.SUBSCRIPTION_PURCHASED else NotificationType.SUBSCRIPTION_RENEWED,
                appMarketplace = AppMarketplace.GOOGLE_PLAY,
                countryCode = subscription.countryCode,
                price = subscription.calculateEffectivePrice(purchaseRequest.effectivePrice),
                currencyCode = subscription.priceCurrencyCode,
                transactionId = subscription.orderId,
                originalTransactionId = subscription.getInitialOrderId(),
                productId = purchaseRequest.subscriptionId,
                description = "Google Play ${if (subscription.isInitialPurchase()) "initial" else "renewal"} subscription order",
                orderingUserId = purchaseRequest.orderingUserId ?: subscription[USER_ACCOUNT_ID_KEY] as String?,
                discountCode = purchaseRequest.discountCode,
                expiryTimeMillis = subscription.expiryTimeMillis,
                googlePlayPurchaseDetails = GooglePlayPurchaseDetails(purchaseRequest.packageName, purchaseRequest.subscriptionId, purchaseRequest.purchaseToken)
        ))

        checkArgument(notificationResponse != null) { "Could not create subscription order ${subscription.orderId} in user app" }

        if (subscription.acknowledgementState == 0) {
            logger.info { "Acknowledging Google Play subscription purchase of id=${subscription.orderId}, purchaseToken=${purchaseRequest.purchaseToken}" }
            val content = SubscriptionPurchasesAcknowledgeRequest().setDeveloperPayload("{ applicationOrderId: ${notificationResponse?.orderId}, orderingUserId: ${purchaseRequest.orderingUserId} }")
            androidPublisherApiClient.Purchases().Subscriptions().acknowledge(purchaseRequest.packageName, purchaseRequest.subscriptionId, purchaseRequest.purchaseToken, content).execute()
        }
        return subscription
    }

    fun cancelPurchase(cancelRequest: SubscriptionCancelRequest) {
        logger.info { "Cancelling subscription: $cancelRequest" }
        try {
            androidPublisherApiClient.purchases().subscriptions().cancel(cancelRequest.packageName, cancelRequest.subscriptionId, cancelRequest.purchaseToken).execute()
        } catch (e: Exception) {
            logger.error(e) { "Error occurred during an attempt to cancel Google Play subscription: $cancelRequest" }
            throw e
        }
    }

    fun handleSubscriptionNotification(pubsubNotification: PubSubDeveloperNotification) {
        pubsubNotification.subscriptionNotification?.let {
            logger.info { "Handling PubSub notification of type: ${it.notificationType}" }
            try {
                when (it.notificationType) {
                    SUBSCRIPTION_PURCHASED -> handlePurchase(PurchaseRequest(pubsubNotification.packageName, it.subscriptionId, it.purchaseToken))
                    SUBSCRIPTION_RENEWED -> handlePurchase(PurchaseRequest(pubsubNotification.packageName, it.subscriptionId, it.purchaseToken))
                    else -> handleStatusUpdate(pubsubNotification.packageName, it)
                }
            } catch (e: Exception) {
                logger.error(e) { "Error handling PubSub notification" }
                throw e
            }
        }
    }

    fun verifyOrders(orders: Collection<PurchaseRequest>): Boolean {
        orders
                .map(::tryToVerifyOrder)
                .also { logger.info { "Verified ${it.size} user orders" } }
        return userAppClient.checkSubscription()?.status == USER_APP_STATUS_ACTIVE
    }

    private fun tryToVerifyOrder(purchaseRequest: PurchaseRequest) = try {
        logger.debug { "About to verify user order: $purchaseRequest" }
        handlePurchase(purchaseRequest)
    } catch (e: Exception) {
        logger.error(e) { "Error during verification of user order" }
    }

    private fun handleStatusUpdate(packageName: String, notification: GooglePlaySubscriptionNotification) {
        val subscription = androidPublisherApiClient.Purchases().Subscriptions().get(packageName, notification.subscriptionId, notification.purchaseToken).execute()
        logger.debug { "Google Play subscription details: $subscription" }
        subscription.cancelReason?.let { logger.info { "Subscription cancel reason: $it" } }
        subscription.cancelSurveyResult?.let { logger.info { "Subscription cancel survey result: $it" } }
        val subscriptionUpdate = UserAppSubscriptionNotification(
                notificationType = NotificationType.valueOf(notification.notificationType.name),
                description = "Google Play subscription update: " + notification.notificationType,
                productId = notification.subscriptionId,
                countryCode = subscription.countryCode,
                price = subscription.calculateEffectivePrice(null),
                currencyCode = subscription.priceCurrencyCode,
                transactionId = subscription.orderId,
                originalTransactionId = subscription.getInitialOrderId(),
                appMarketplace = AppMarketplace.GOOGLE_PLAY,
                expiryTimeMillis = subscription.expiryTimeMillis,
                googlePlayPurchaseDetails = GooglePlayPurchaseDetails(packageName, notification.subscriptionId, notification.purchaseToken)
        )
        userAppClient.sendSubscriptionNotification(subscriptionUpdate)
        logger.info { "Google Play subscription notification has been sent to user app: $subscriptionUpdate" }
    }

    private fun SubscriptionPurchase.calculateEffectivePrice(effectivePrice: Long?) = when {
        isTestPurchase() -> 0L
        effectivePrice != null -> effectivePrice
        isIntroductoryPricePurchase() -> introductoryPriceInfo.introductoryPriceAmountMicros
        else -> priceAmountMicros
    }
            .let(::BigDecimal)
            .let { it.divide(BigDecimal(1000 * 1000)) }

    private fun SubscriptionPurchase.isTestPurchase() = purchaseType == 0

    private fun SubscriptionPurchase.isInitialPurchase() = !orderId.contains("..")

    private fun SubscriptionPurchase.isIntroductoryPricePurchase() = isInitialPurchase() && introductoryPriceInfo?.introductoryPriceAmountMicros != null

    private fun SubscriptionPurchase.getInitialOrderId() = orderId?.let { it.split("..")[0] } ?: ""

}