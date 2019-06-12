package com.dietmap.yaak.api.receipt

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class ReceiptRequest(
        @get:JsonProperty("receipt-data") @param:NotBlank val receiptData: String,
        @get:JsonProperty("password") @param:NotBlank val password: String,
        @get:JsonProperty("exclude-old-transactions") @param:NotNull val excludeOldTransactions : Boolean = false
): Serializable