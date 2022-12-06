package com.dietmap.yaak.domain.userapp

import java.math.BigDecimal
import javax.validation.constraints.NotNull

data class UserAppSubscriptionNotification(
        @NotNull
        val notificationType: NotificationType,
        val description: String? = String(),
        @NotNull
        val productId: String,
        @NotNull
        var countryCode: String? = String(),
        @NotNull
        var price: BigDecimal? = BigDecimal.ZERO,
        @NotNull
        val currencyCode: String? = String(),
        @NotNull
        val transactionId: String,
        @NotNull
        val originalTransactionId: String? = String(),
        @NotNull
        val appMarketplace: AppMarketplace,
        val expiryTimeMillis: Long? = 0,
        val orderingUserId: String? = String(),
        val discountCode: String? = String(),
        val appStoreReceipt: String? = String(),
        val googlePlayPurchaseDetails: GooglePlayPurchaseDetails? = null,
        val isTrialPeriod: Boolean? = false
)

enum class NotificationType {
    /**
     * A subscription was recovered from account hold.
     */
    SUBSCRIPTION_RECOVERED,
    /**
     * An active subscription was renewed.
     */
    SUBSCRIPTION_RENEWED,
    /**
     * A subscription was either voluntarily or involuntarily cancelled. For voluntary cancellation, sent when the user cancels.
     */
    SUBSCRIPTION_CANCELED,
    /**
     * A new subscription was purchased.
     */
    SUBSCRIPTION_PURCHASED,
    /**
     * A subscription has entered account hold (if enabled).
     */
    SUBSCRIPTION_ON_HOLD,
    /**
     * A subscription has entered grace period (if enabled).
     */
    SUBSCRIPTION_IN_GRACE_PERIOD,
    /**
     * User has reactivated their subscription from Play > Account > Subscriptions (requires opt-in for subscription restoration)
     */
    SUBSCRIPTION_RESTARTED,
    /**
     * A subscription price change has successfully been confirmed by the user.
     */
    SUBSCRIPTION_PRICE_CHANGE_CONFIRMED,
    /**
     * A subscription's recurrence time has been extended.
     */
    SUBSCRIPTION_DEFERRED,
    /**
     * A subscription has been paused.
     */
    SUBSCRIPTION_PAUSED,
    /**
     * A subscription pause schedule has been changed.
     */
    SUBSCRIPTION_PAUSE_SCHEDULE_CHANGED,
    /**
     * A subscription has been revoked from the user before the expiration time.
     */
    SUBSCRIPTION_REVOKED,
    /**
     * A subscription has expired.
     */
    SUBSCRIPTION_EXPIRED,
    /**
     * A subscription renewal failed due to payment issue
     */
    SUBSCRIPTION_RENEW_FAILED
}


data class UserAppSubscriptionOrder (
        val orderId: String?,
        val transactionId: String?,
        val status: String?
)

data class UserAppSubscriptionStatus(
        val status: String
)

data class GooglePlayPurchaseDetails(
        val packageName: String,
        val subscriptionId: String,
        val purchaseToken: String
)

enum class AppMarketplace {
    GOOGLE_PLAY, APP_STORE
}