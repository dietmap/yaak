package com.dietmap.yaak.domain.appstore

import com.dietmap.yaak.api.appstore.receipt.ReceiptRequest
import com.dietmap.yaak.api.appstore.subscription.AppStoreNotificationType
import com.dietmap.yaak.api.appstore.subscription.StatusUpdateNotification
import com.dietmap.yaak.api.appstore.subscription.SubscriptionPurchaseRequest
import com.dietmap.yaak.api.appstore.subscription.SubscriptionRenewRequest
import com.dietmap.yaak.domain.checkArgument
import com.dietmap.yaak.domain.userapp.*
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("yaak.app-store.enabled", havingValue = "true")
class AppStoreSubscriptionService(val userAppClient: UserAppClient, val appStoreClient: AppStoreClient) {

    private val logger = KotlinLogging.logger { }

    fun handleInitialPurchase(subscriptionPurchaseRequest: SubscriptionPurchaseRequest) : UserAppSubscriptionOrder? {
        val receiptResponse = appStoreClient.verifyReceipt(ReceiptRequest(subscriptionPurchaseRequest.receipt))

        logger.debug { "handleInitialPurchase: ReceiptResponse: $receiptResponse" }

        if (receiptResponse.isValid()) {

            val latestReceiptInfo = receiptResponse.latestReceiptInfo!!.stream().findFirst().get()

            var effectivePrice = subscriptionPurchaseRequest.price

            // intro offer period purchase
            if (latestReceiptInfo.isInIntroOfferPeriod && subscriptionPurchaseRequest.effectivePrice != null) {
                effectivePrice = subscriptionPurchaseRequest.effectivePrice
            }

            val notification = UserAppSubscriptionNotification(
                    notificationType = NotificationType.SUBSCRIPTION_PURCHASED,
                    description = "Subscription purchase from AppStore",
                    productId = latestReceiptInfo.productId,
                    countryCode = subscriptionPurchaseRequest.countryCode,
                    price = effectivePrice,
                    currencyCode = subscriptionPurchaseRequest.currencyCode,
                    transactionId = latestReceiptInfo.transactionId,
                    originalTransactionId = latestReceiptInfo.originalTransactionId,
                    appMarketplace = AppMarketplace.APP_STORE,
                    expiryTimeMillis = latestReceiptInfo.expiresDateMs,
                    discountCode = subscriptionPurchaseRequest.discountCode,
                    appStoreReceipt = subscriptionPurchaseRequest.receipt
            )

            return userAppClient.sendSubscriptionNotification(notification)
        } else {
            val message = "The ${receiptResponse.receipt} is not a valid receipt. Response code ${receiptResponse.responseStatusCode}"
            logger.error { message }
            throw ReceiptValidationException(message)
        }
    }

    fun handleAutoRenewal(subscriptionRenewRequest: SubscriptionRenewRequest) {
        val receiptResponse = appStoreClient.verifyReceipt(ReceiptRequest(subscriptionRenewRequest.receipt))

        logger.debug { "handleAutoRenewal: ReceiptResponse: $receiptResponse" }

        if (receiptResponse.isValid()) {

            if (receiptResponse.latestReceiptInfo != null) {

                val latestReceiptInfo = receiptResponse.latestReceiptInfo.stream().findFirst().get()

                val notification = UserAppSubscriptionNotification(
                        notificationType = NotificationType.SUBSCRIPTION_RENEWED,
                        description = "Subscription renewal from AppStore",
                        productId = latestReceiptInfo.productId,
                        transactionId = latestReceiptInfo.transactionId,
                        originalTransactionId = latestReceiptInfo.originalTransactionId,
                        appMarketplace = AppMarketplace.APP_STORE,
                        expiryTimeMillis = latestReceiptInfo.expiresDateMs,
                        countryCode = null,
                        currencyCode = null,
                        discountCode = subscriptionRenewRequest.discountCode,
                        appStoreReceipt = subscriptionRenewRequest.receipt
                )

                val subscriptionOrder = userAppClient.sendSubscriptionNotification(notification);

                checkArgument(subscriptionOrder != null) { "Could not process SubscriptionRenewRequest $subscriptionRenewRequest in user app" }
            }
        } else {
            val message = "The ${receiptResponse.receipt} is not a valid receipt. Response code ${receiptResponse.responseStatusCode}"
            logger.error { message }
            throw ReceiptValidationException(message)
        }
    }

    fun handleSubscriptionNotification(statusUpdateNotification: StatusUpdateNotification) : UserAppSubscriptionOrder? {
        logger.debug { "Processing StatusUpdateNotification: ${statusUpdateNotification.notificationType}" }

        var notificationType = NotificationType.SUBSCRIPTION_PURCHASED
        val latestReceiptInfo = statusUpdateNotification.latestReceiptInfo

        when (statusUpdateNotification.notificationType) {

            // A subscription is first purchased
            AppStoreNotificationType.INITIAL_BUY -> {
                // skipping it
            }

            // a subscription is renewed manually in the foreground
            AppStoreNotificationType.INTERACTIVE_RENEWAL -> {

                notificationType = NotificationType.SUBSCRIPTION_RENEWED

                // latest_receipt_info.purchase_date - resubscribe
                // latest_receipt_info.original_transaction_id
                // latest_receipt_info.product_id
            }

            // a customer downgrades
            AppStoreNotificationType.DID_CHANGE_RENEWAL_PREF -> {
                // auto_renewal_product_id - product customer will auto renew at

                // latest_receipt_info.original_transaction_id
            }

            // customer support issues a refund
            AppStoreNotificationType.CANCEL -> {
                // suspend service with a cancellation date?

                notificationType = NotificationType.SUBSCRIPTION_CANCELED

                // cancellation_date_ms - date/time of cancellation
                // latest_receipt_info.product_id
                // latest_receipt_info.original_transaction_id
            }

            AppStoreNotificationType.DID_FAIL_TO_RENEW -> {
                // optionally choose to suspend service
                // update subscription status to "inactive" or "billing-retry"

                notificationType = NotificationType.SUBSCRIPTION_RENEW_FAILED

                // pending_renewal_info.is_in_billing_period - whether the appstore is trying to renew the subscription
                // latest_receipt_info.original_transaction_id
                // latest_receipt_info.expires_date_ms - date when the subscription expired
            }

            AppStoreNotificationType.DID_RECOVER -> {
                // restore service for a recovered subscription
                // update customer's subscription to active / subscribe

                notificationType = NotificationType.SUBSCRIPTION_RECOVERED

                // latest_receipt_info.purchase_date_ms - date of recovery
                // latest_receipt_info.original_transaction_id
                // latest_receipt_info.expires_date_ms - date when the subscription will expire
            }

            AppStoreNotificationType.DID_CHANGE_RENEWAL_STATUS -> {
                // skipping it
            }

            AppStoreNotificationType.PRICE_INCREASE_CONSENT -> {
                // skipping it
            }
        }

        val notification = UserAppSubscriptionNotification(
                notificationType = notificationType,
                description = "Subscription update from AppStore: ${statusUpdateNotification.notificationType}",
                productId = latestReceiptInfo.productId,
                transactionId = latestReceiptInfo.transactionId,
                originalTransactionId = latestReceiptInfo.originalTransactionId,
                appMarketplace = AppMarketplace.APP_STORE,
                expiryTimeMillis = latestReceiptInfo.expiresDateMs,
                countryCode = null,
                currencyCode = null,
                discountCode = null,
                appStoreReceipt = statusUpdateNotification.latestReceipt
        )

        logger.debug {"Sending UserAppSubscriptionNotification: $notification" }

        return userAppClient.sendSubscriptionNotification(notification)

    }
}