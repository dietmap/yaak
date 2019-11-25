package com.dietmap.yaak.api.appstore.receipt

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
                           @get:JsonProperty("receipt") val receipt: Any?,
                           @get:JsonProperty("latest_receipt") val latestReceipt: String?,
                           @get:JsonProperty("latest_receipt_info") val latestReceiptInfo: Any?,
                           @get:JsonProperty("pending_renewal_info") val pendingRenewalInfo: Any?,
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