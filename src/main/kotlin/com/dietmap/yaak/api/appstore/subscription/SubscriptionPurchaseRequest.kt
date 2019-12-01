package com.dietmap.yaak.api.appstore.subscription

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.math.BigDecimal
import javax.validation.constraints.NotBlank

data class SubscriptionPurchaseRequest(
        @get:JsonProperty("receipt") @param:NotBlank val receipt: String,
        @get:JsonProperty("price") @param:NotBlank val price: BigDecimal,
        @get:JsonProperty("currencyCode") @param:NotBlank val currencyCode: String,
        @get:JsonProperty("countryCode") @param:NotBlank val countryCode: String
): Serializable