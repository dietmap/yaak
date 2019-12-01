package com.dietmap.yaak.domain.userapp

import java.math.BigDecimal
import javax.validation.constraints.NotNull

data class UserAppSubscriptionNotification(
        @NotNull
        val notificationType: NotificationType,
        val description: String? = "",
        @NotNull
        val productId: String,
        @NotNull
        val countryCode: String? = "",
        @NotNull
        val price: BigDecimal = BigDecimal.ZERO,
        @NotNull
        val currencyCode: String? = "",
        @NotNull
        val transactionId: String,
        @NotNull
        val originalTransactionId: String? = "",
        @NotNull
        val appMarketplace: AppMarketplace,
        val expiryTimeMillis: Long? = 0,
        val orderingUserId: String? = ""
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
    SUBSCRIPTION_EXPIRED
}


data class UserAppSubscriptionOrder(
        val orderId: String,
        val transactionId: String,
        val status: UserAppSubscriptionStatus
)

enum class UserAppSubscriptionStatus {
    WAITING_FOR_PAYMENT, PROCESSING_PAYMENT, PAYMENT_FAILED, COMPLETED, CANCELED, REFUNDED
}

enum class AppMarketplace {
    GOOGLE_PLAY, APP_STORE
}