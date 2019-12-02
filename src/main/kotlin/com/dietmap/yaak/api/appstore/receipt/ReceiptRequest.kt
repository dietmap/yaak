package com.dietmap.yaak.api.appstore.receipt

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull


/**
 * This class represents receipt request as defined in https://developer.apple.com/documentation/appstorereceipts/requestbody
 **/
data class ReceiptRequest(
        @get:JsonProperty("receipt-data") @param:NotBlank val receipt: String,
        @get:JsonProperty("password") @param:NotBlank var password: String = String(),
        @get:JsonProperty("exclude-old-transactions") @param:NotNull val excludeOldTransactions : Boolean? = true
): Serializable {
    override fun toString(): String {
        return "ReceiptRequest(receipt=$receipt, excludeOldTransactions='$excludeOldTransactions')"
    }
}