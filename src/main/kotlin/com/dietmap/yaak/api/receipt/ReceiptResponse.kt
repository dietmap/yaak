package com.dietmap.yaak.api.receipt

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class ReceiptResponse(@field:JsonProperty("status") val status: Int,
                           @field:JsonProperty("environment") val environment: String,
                           @field:JsonProperty("receipt") val receipt: String,
                           @field:JsonProperty("latest_receipt") val latestReceipt: String,
                           @field:JsonProperty("latest_receipt_info") val latestReceiptInfo: String,
                           @field:JsonProperty("latest_expired_receipt_info") val latestExpiredReceiptInfo: String,
                           @field:JsonProperty("pending_renewal_info") val pendingRenewalInfo: String,
                           @field:JsonProperty("is-retryable") val isRetryable: Boolean
): Serializable {

    @JsonProperty("status_info")
    val responseStatusCode : ResponseStatusCode? = ResponseStatusCode.getByCode(status)
}