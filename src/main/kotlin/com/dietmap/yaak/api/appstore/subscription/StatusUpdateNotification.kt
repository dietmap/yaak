package com.dietmap.yaak.api.appstore.subscription

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class StatusUpdateNotification(
        @get:JsonProperty("environment") val environment: String,
        @get:JsonProperty("notification_type") val notificationType: NotificationStatus,
        @get:JsonProperty("password") val password: String,
        @get:JsonProperty("cancellation_date") val cancellationDate: String,
        @get:JsonProperty("cancellation_date_pst") val cancellationDatePst: String,
        @get:JsonProperty("cancellation_date_ms") val cancellationDateMs: String,
        @get:JsonProperty("web_order_line_item_id") val webOrderLineItemId: String,
        @get:JsonProperty("latest_receipt") val latestReceipt: String,
        @get:JsonProperty("latest_receipt_info") val latestReceiptInfo: String,
        @get:JsonProperty("latest_expired_receipt") val latestExpiredReceipt: String,
        @get:JsonProperty("latest_expired_receipt_info") val latestExpiredReceiptInfo: String,
        @get:JsonProperty("auto_renew_status") val autoRenewStatus: Boolean,
        @get:JsonProperty("auto_renew_product_id") val autoRenewProductId: String,
        @get:JsonProperty("auto_renew_status_change_date") val autoRenewStatusChangeDate: String,
        @get:JsonProperty("auto_renew_status_change_date_pst") val autoRenewStatusChangeDatePst: String,
        @get:JsonProperty("auto_renew_status_change_date_ms") val autoRenewStatusChangeDateMs: String
) : Serializable