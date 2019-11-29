package com.dietmap.yaak.api.appstore.receipt

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

/**
 * This class represents receipt response as defined in https://developer.apple.com/documentation/appstorereceipts/responsebody
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ReceiptResponse(@get:JsonProperty("status") val status: Int,
                           @get:JsonProperty("environment") val environment: String,
                           @get:JsonProperty("receipt") val receipt: Receipt,
                           @get:JsonProperty("latest_receipt") val latestReceipt: Any?,
                           @get:JsonProperty("latest_receipt_info") val latestReceiptInfo: LatestReceiptInfo,
                           @get:JsonProperty("pending_renewal_info") val pendingRenewalInfo: PendingRenewalInfo,
                           @get:JsonProperty("is-retryable") val isRetryable: Boolean = false
): Serializable {

    @JsonProperty("status_info")
    val responseStatusCode : ResponseStatusCode? = ResponseStatusCode.getByCode(status)

    @JsonIgnore
    fun isValid() : Boolean = status == 0

    @JsonIgnore
    fun shouldRetry() : Boolean = (status != 0 && isRetryable)

    override fun toString(): String {
        return "ReceiptResponse(status=$status, environment='$environment', responseStatusCode=$responseStatusCode)"
    }
}

/**
 * This class represents receipt as defined in https://developer.apple.com/documentation/appstorereceipts/responsebody/receipt
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Receipt (
        @get:JsonProperty("adam_id") val adamId: Int,
        @get:JsonProperty("app_item_id") val appItemId: Int,
        @get:JsonProperty("application_version") val applicationVersion: String,
        @get:JsonProperty("bundle_id") val bundleId: String,
        @get:JsonProperty("download_id") val downloadId: Int,
        @get:JsonProperty("expiration_date") val expirationDate: String,
        @get:JsonProperty("expiration_date_ms") val expirationDateMs: String,
        @get:JsonProperty("expiration_date_pst") val expirationDatePst: String,
        @get:JsonProperty("in_app") val inApp: Any?,
        @get:JsonProperty("original_application_version") val originalApplicationVersion: String,
        @get:JsonProperty("original_purchase_date") val originalPurchaseDate: String,
        @get:JsonProperty("original_purchase_date_ms") val originalPurchaseDateMs: String,
        @get:JsonProperty("original_purchase_date_pst") val originalPurchaseDatePst: String,
        @get:JsonProperty("preorder_date") val preorderDate: String,
        @get:JsonProperty("preorder_date_ms") val preorderDateMs: String,
        @get:JsonProperty("preorder_date_pst") val preorderDatePst: String,
        @get:JsonProperty("receipt_creation_date") val receiptCreationDate: String,
        @get:JsonProperty("receipt_creation_date_ms") val receiptCreationDateMs: String,
        @get:JsonProperty("receipt_creation_date_pst") val receiptCreationDatePst: String,
        @get:JsonProperty("receipt_type") val receiptType: String,
        @get:JsonProperty("request_date") val requestDate: String,
        @get:JsonProperty("request_date_ms") val requestDateMs: String,
        @get:JsonProperty("request_date_pst") val requestDatePst: String,
        @get:JsonProperty("version_external_identifier") val versionExternalIdentifier: String
): Serializable {

}

/**
 * This class represents latest_receipt_info as defined in https://developer.apple.com/documentation/appstorereceipts/responsebody/latest_receipt_info
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
data class LatestReceiptInfo (
        @get:JsonProperty("cancellation_date") val cancellationDate: String,
        @get:JsonProperty("cancellation_date_ms") val cancellationDateMs: Int,
        @get:JsonProperty("cancellation_reason") val cancellationReason: String,
        @get:JsonProperty("expires_date") val expiresDate: String,
        @get:JsonProperty("expires_date_ms") val expiresDateMs: String,
        @get:JsonProperty("expires_date_pst") val expiresDatePst: String,
        @get:JsonProperty("is_in_intro_offer_period") val isInIntroOfferPeriod: String,
        @get:JsonProperty("is_trial_period") val isTrialPeriod: Boolean,
        @get:JsonProperty("is_upgraded") val isUpgraded: Boolean?,
        @get:JsonProperty("original_purchase_date") val originalPurchaseDate: String,
        @get:JsonProperty("original_purchase_date_ms") val originalPurchaseDateMs: String,
        @get:JsonProperty("original_purchase_date_pst") val originalPurchaseDatePst: String,
        @get:JsonProperty("original_transaction_id") val originalTransactionId: String,
        @get:JsonProperty("product_id") val productId: String,
        @get:JsonProperty("promotional_offer_id") val promotionalOfferId: String,
        @get:JsonProperty("purchase_date") val purchaseDate: String,
        @get:JsonProperty("purchase_date_ms") val purchaseDateMs: String,
        @get:JsonProperty("purchase_date_pst") val purchaseDatePst: String,
        @get:JsonProperty("quantity") val quantity: String,
        @get:JsonProperty("subscription_group_identifier") val subscriptionGroupIdentifier: String,
        @get:JsonProperty("transaction_id") val transactionId: String,
        @get:JsonProperty("web_order_line_item_id") val webOrderLineItemId: String
): Serializable {

}


/**
 * This class represents Pending_renewal_info as defined in https://developer.apple.com/documentation/appstorereceipts/responsebody/pending_renewal_info
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PendingRenewalInfo (
        @get:JsonProperty("auto_renew_product_id") val autoRenewProductId: String,
        @get:JsonProperty("auto_renew_status") val autoRenewStatus: Int,
        @get:JsonProperty("expiration_intent") val expirationIntent: Int,
        @get:JsonProperty("grace_period_expires_date") val gracePeriodExpiresDate: String,
        @get:JsonProperty("grace_period_expires_date_ms") val gracePeriodExpiresDateMs: String,
        @get:JsonProperty("grace_period_expires_date_pst") val gracePeriodExpiresDatePst: String,
        @get:JsonProperty("original_transaction_id") val originalTransactionId: String,
        @get:JsonProperty("is_in_billing_retry_period") val isInBillingRetryPeriod: Int,
        @get:JsonProperty("price_consent_status") val priceConsentStatus: String?,
        @get:JsonProperty("product_id") val productId: String
): Serializable {

}

/**
 * This class represents status codes as defined in https://developer.apple.com/documentation/appstorereceipts/status
 **/
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class ResponseStatusCode(private val code: Int,
                              private val description: String) : Serializable {

    CODE_0(0, "The receipt is valid"),

    CODE_21000(21000, "The request to the App Store was not made using the HTTP POST request method"),
    CODE_21001(21001, "This status code is no longer sent by the App Store"),
    CODE_21002(21002, "The data in the receipt-data property was malformed or missing"),
    CODE_21003(21003, "The receipt could not be authenticated"),
    CODE_21004(21004, "The shared secret you provided does not match the shared secret on file for your account"),
    CODE_21005(21005, "The receipt server is not currently available"),
    CODE_21006(21006, "This receipt is valid but the subscription has expired. When this status code is returned to your server,the receipt data is also decoded and returned as part of the response"),
    CODE_21007(21007, "This receipt is from the test environment, but it was sent to the production environment for verification"),
    CODE_21008(21008, "This receipt is from the production environment, but it was sent to the test environment for verification"),
    CODE_21010(21009, "Internal data access error. Try again later."),
    CODE_21100(21010, "The user account cannot be found or has been deleted");

    companion object {
        @JvmStatic
        fun getByCode(code: Int) : ResponseStatusCode? = values().firstOrNull { e -> e.code == code }
    }

    @JsonProperty
    fun getDescription() : String = description

    @JsonProperty
    fun getCode() : Int = code

    override fun toString(): String {
        return "ResponseStatusCode(code=$code, description='$description')"
    }
}