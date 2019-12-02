package com.dietmap.yaak.domain.appstore

import com.dietmap.yaak.api.appstore.receipt.ReceiptRequest
import com.dietmap.yaak.api.appstore.subscription.AppStoreNotificationType
import com.dietmap.yaak.api.appstore.subscription.StatusUpdateNotification
import com.dietmap.yaak.api.appstore.subscription.SubscriptionPurchaseRequest
import com.dietmap.yaak.api.appstore.subscription.SubscriptionRenewRequest
import com.dietmap.yaak.domain.userapp.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty("yaak.app-store.enabled", havingValue = "true")
class AppStoreSubscriptionService(val userAppClient: UserAppClient, val appStoreClient: AppStoreClient) {

    private val logger: Logger = LoggerFactory.getLogger(AppStoreSubscriptionService::class.java)

    fun handleInitialPurchase(subscriptionPurchaseRequest: SubscriptionPurchaseRequest) : UserAppSubscriptionOrder? {
        val receiptResponse = appStoreClient.verifyReceipt(ReceiptRequest(subscriptionPurchaseRequest.receipt))

        if (receiptResponse.isValid()) {

            // TODO find out which one of latestReceiptInfo
            val latestReceiptInfo = receiptResponse.latestReceiptInfo.stream().findFirst().get()

            val notification = UserAppSubscriptionNotification(
                    notificationType = NotificationType.SUBSCRIPTION_PURCHASED,
                    productId = latestReceiptInfo.productId,
                    transactionId = latestReceiptInfo.transactionId,
                    price = subscriptionPurchaseRequest.price,
                    countryCode = subscriptionPurchaseRequest.countryCode,
                    currencyCode = subscriptionPurchaseRequest.currencyCode,
                    appMarketplace = AppMarketplace.APP_STORE,
                    description = "Subscription purchase from AppStore",
                    expiryTimeMillis = latestReceiptInfo.expiresDateMs.toLong()
            )

            return userAppClient.sendSubscriptionNotification(notification)
        } else {
            throw ReceiptValidationException("The ${receiptResponse.receipt} is not a valid receipt. " +
                    "Response code ${receiptResponse.responseStatusCode}")
        }
    }

    fun handleAutoRenewal(subscriptionRenewRequest: SubscriptionRenewRequest) : UserAppSubscriptionOrder? {
        val receiptResponse = appStoreClient.verifyReceipt(ReceiptRequest(subscriptionRenewRequest.receipt))

        if (receiptResponse.isValid()) {

            // TODO find out which one of latestReceiptInfo
            val latestReceiptInfo = receiptResponse.latestReceiptInfo.stream().findFirst().get()

            val notification = UserAppSubscriptionNotification(
                    notificationType = NotificationType.SUBSCRIPTION_RENEWED,
                    productId = latestReceiptInfo.productId,
                    transactionId = latestReceiptInfo.transactionId,
                    originalTransactionId = latestReceiptInfo.originalTransactionId,
                    appMarketplace = AppMarketplace.APP_STORE,
                    description = "Subscription renewal from AppStore",
                    expiryTimeMillis = latestReceiptInfo.expiresDateMs.toLong()
            )

            return userAppClient.sendSubscriptionNotification(notification)
        } else {
            throw ReceiptValidationException("The ${receiptResponse.receipt} is not a valid receipt. " +
                    "Response code ${receiptResponse.responseStatusCode}")
        }
    }

    fun handleSubscriptionNotification(statusUpdateNotification: StatusUpdateNotification) : UserAppSubscriptionOrder? {
        logger.debug("Processing notification: ${statusUpdateNotification.notificationType}")

        var notificationType = NotificationType.SUBSCRIPTION_PURCHASED
        val latestReceiptInfo = statusUpdateNotification.latestReceiptInfo

        when (statusUpdateNotification.notificationType) {

            // A subscription is first purchased
            AppStoreNotificationType.INITIAL_BUY -> {
                // skipping it, this is handled in handleInitialPurchase()
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

                // TODO clarify this notification
                // Ignore?

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
                productId = latestReceiptInfo.productId,
                transactionId = latestReceiptInfo.originalTransactionId,
                appMarketplace = AppMarketplace.APP_STORE,
                description = "Subscription update from AppStore: ${statusUpdateNotification.notificationType}",
                expiryTimeMillis = latestReceiptInfo.expiresDateMs.toLong()
        )

        return userAppClient.sendSubscriptionNotification(notification)

    }
}