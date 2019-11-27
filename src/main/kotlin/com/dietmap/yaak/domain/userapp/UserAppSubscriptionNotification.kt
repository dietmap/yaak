package com.dietmap.yaak.domain.userapp

import java.math.BigDecimal
import javax.validation.constraints.NotNull

data class UserAppSubscriptionNotification(
        @NotNull
        val notificationType: NotificationType,
        var description: String?,
        @NotNull
        var orderingUserInternalId: Int,
        @NotNull
        var productId: String,
        @NotNull
        var countryCode: String,
        @NotNull
        var price: BigDecimal,
        @NotNull
        val currencyCode: String,
        @NotNull
        var transactionId: String,
        @NotNull
        var appMarketplace: AppMarketplace
)

enum class NotificationType {
    /**
     * Occurs at the initial purchase of the subscription. Store the latest_receipt on your server
     */
    INITIAL_BUY,
    /**
     * Indicates that the subscription was canceled either by Apple customer support or by the
     * App Store when the user upgraded their subscription.
     * The cancellation_date key contains the date and time when the subscription was canceled or upgraded.
     */
    CANCEL,
    /**
     * Indicates successful automatic renewal of an expired subscription that failed to renew in the past.
     * Check expires_date to determine the next renewal date and time.
     */
    RENEWAL,
    /**
     * Indicates the customer renewed a subscription interactively
     * either by using your app’s interface, or on the App Store in account settings. Make service available immediately.
     */
    INTERACTIVE_RENEWAL,
    /**
     * Indicates the customer made a change in their subscription plan that
     * "takes effect at the next renewal. The currently active plan is not affected.
     */
    DID_CHANGE_RENEWAL_PREF,
    /**
     * Indicates a change in the subscription renewal status. Check the
     * auto_renew_status_change_date_ms and the auto_renew_status in the JSON to know the date and time
     * when the status was last updated and the current renewal status.
     */
    DID_CHANGE_RENEWAL_STATUS
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