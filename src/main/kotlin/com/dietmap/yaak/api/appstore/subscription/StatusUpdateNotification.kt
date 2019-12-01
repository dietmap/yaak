package com.dietmap.yaak.api.appstore.subscription

import com.dietmap.yaak.api.appstore.receipt.LatestReceiptInfo
import com.dietmap.yaak.api.appstore.receipt.PendingRenewalInfo
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class StatusUpdateNotification(
        @get:JsonProperty("environment") val environment: String,
        @get:JsonProperty("notification_type") val notificationType: AppStoreNotificationType,
        @get:JsonProperty("password") val password: String,
        @get:JsonProperty("latest_receipt") val latestReceipt: String,
        @get:JsonProperty("latest_receipt_info") val latestReceiptInfo: LatestReceiptInfo,
        @get:JsonProperty("latest_expired_receipt") val latestExpiredReceipt: String,
        @get:JsonProperty("latest_expired_receipt_info") val latestExpiredReceiptInfo: List<String>,
        @get:JsonProperty("expiration_intent") val expirationIntent: String,
        @get:JsonProperty("auto_renew_adam_id") val autoRenewAdamId: String,
        @get:JsonProperty("auto_renew_status") val autoRenewStatus: Boolean,
        @get:JsonProperty("auto_renew_product_id") val autoRenewProductId: String,
        @get:JsonProperty("auto_renew_status_change_date") val autoRenewStatusChangeDate: String,
        @get:JsonProperty("auto_renew_status_change_date_pst") val autoRenewStatusChangeDatePst: String,
        @get:JsonProperty("auto_renew_status_change_date_ms") val autoRenewStatusChangeDateMs: String,
        @get:JsonProperty("unified_receipt") val unifiedReceipt: UnifiedReceipt
) : Serializable


data class UnifiedReceipt(
        @get:JsonProperty("environment") val environment: String,
        @get:JsonProperty("latest_receipt") val latestReceipt: String,
        @get:JsonProperty("latest_receipt_info") val latestReceiptInfo: List<LatestReceiptInfo>,
        @get:JsonProperty("pending_renewal_info") val pendingRenewalInfo: List<PendingRenewalInfo>,
        @get:JsonProperty("status") val latestExpiredReceiptInfo: Int
) : Serializable


/**
 * This class represents notification_type as defined in https://developer.apple.com/documentation/appstoreservernotifications/notification_type
 **/
enum class AppStoreNotificationType(private val code: Int) {
    /**
     * Indicates that either Apple customer support canceled the subscription or the user upgraded their subscription.
     * The cancellation_date key contains the date and time of the change.
     */
    CANCEL(1),

    /**
     * Indicates the customer made a change in their subscription plan that takes effect at the next renewal. The currently active plan is not affected.
     */
    DID_CHANGE_RENEWAL_PREF(2),

    /**
     * Indicates a change in the subscription renewal status. Check auto_renew_status_change_date_ms and
     * auto_renew_status in the JSON response to know the date and time of the last status update and the current renewal status.
     */
    DID_CHANGE_RENEWAL_STATUS(3),

    /**
     * Indicates a subscription that failed to renew due to a billing issue.
     * Check is_in_billing_retry_period to know the current retry status of the subscription,
     * and grace_period_expires_date to know the new service expiration date if the subscription is in a billing grace period
     */
    DID_FAIL_TO_RENEW(4),

    /**
     * Indicates successful automatic renewal of an expired subscription that failed to renew in the past.
     * Check expires_date to determine the next renewal date and time.
     */
    DID_RECOVER(5),

    /**
     * Occurs at the initial purchase of the subscription. Store latest_receipt on your server as a token
     * to verify the user’s subscription status at any time, by validating it with the App Store.
     */
    INITIAL_BUY(6),

    /**
     * Indicates the customer renewed a subscription interactively, either by using your app’s interface,
     * or on the App Store in the account's Subscriptions settings. Make service available immediately.
     */
    INTERACTIVE_RENEWAL(7),

    /**
     * User has entered a price increase flow
     */
    PRICE_INCREASE_CONSENT(8);

    companion object {
        private val codes = values().associateBy(AppStoreNotificationType::code)
        @JvmStatic
        @JsonCreator
        fun from(value: Int) = codes[value]
    }
}