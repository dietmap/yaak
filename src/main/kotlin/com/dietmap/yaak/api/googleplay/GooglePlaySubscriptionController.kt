package com.dietmap.yaak.api.googleplay

import com.dietmap.yaak.domain.googleplay.GooglePlaySubscriptionService
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import org.apache.commons.codec.binary.Base64
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper



@ConditionalOnProperty("yaak.google-play.enabled", havingValue = "true")
@RestController
class GooglePlaySubscriptionController(val subscriptionService: GooglePlaySubscriptionService) {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/api/googleplay/subscriptions/purchases")
    fun purchase(@RequestBody @Valid purchaseRequest: PurchaseRequest): SubscriptionPurchase? {
        logger.info("Received purchase request from Google Play: {}", purchaseRequest)
        return subscriptionService.handlePurchase(purchaseRequest.packageName, purchaseRequest.subscriptionId, purchaseRequest.purchaseToken)
    }

    /**
     * Publicly accessible PubSub notification webhook
     */
    @PostMapping("/public/api/googleplay/subscriptions/notifications")
    fun update(@RequestBody pubsubRequest: PubSubRequest) {
        logger.info("Received Google PubSub subscription notification: {}", pubsubRequest)
        subscriptionService.handleSubscriptionNotification(pubsubRequest.message.developerNotification)
    }
}

data class PurchaseRequest(
        @NotBlank
        val packageName: String,
        @NotBlank
        val subscriptionId: String,
        @NotBlank
        val purchaseToken: String
)

data class PubSubRequest(
        val subscription: String,
        val message: PubSubMessage
)

data class PubSubMessage(val messageId: String, val data: String) {
    val developerNotification: PubSubDeveloperNotification

    init {
        val dataDecoded = Base64.decodeBase64(data)
        logger.info("Decoded PubSub message: $dataDecoded");
        val mapper = jacksonObjectMapper()
        this.developerNotification = mapper.readValue(dataDecoded, PubSubDeveloperNotification::class.java)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun toString(): String {
        return "PubSubMessage(messageId='$messageId', developerNotification=$developerNotification)"
    }
}

data class PubSubDeveloperNotification(
        val version: String,
        val packageName: String,
        val eventTimeMillis: Long,
        val subscriptionNotification: GooglePlaySubscriptionNotification?,
        val testNotification: GooglePlayTestNotification?
)

data class GooglePlayTestNotification(
        val version: String
)

data class GooglePlaySubscriptionNotification(
        val version: String,
        val notificationType: GooglePlayNotificationType,
        val purchaseToken: String,
        val subscriptionId: String
)

enum class GooglePlayNotificationType(private val code: Int) {
    /**
     * A subscription was recovered from account hold.
     */
    SUBSCRIPTION_RECOVERED(1),
    /**
     * An active subscription was renewed.
     */
    SUBSCRIPTION_RENEWED(2),
    /**
     * A subscription was either voluntarily or involuntarily cancelled. For voluntary cancellation, sent when the user cancels.
     */
    SUBSCRIPTION_CANCELED(3),
    /**
     * A new subscription was purchased.
     */
    SUBSCRIPTION_PURCHASED(4),
    /**
     * A subscription has entered account hold (if enabled).
     */
    SUBSCRIPTION_ON_HOLD(5),
    /**
     *A subscription has entered grace period (if enabled).
     */
    SUBSCRIPTION_IN_GRACE_PERIOD(6),
    /**
     * User has reactivated their subscription from Play > Account > Subscriptions (requires opt-in for subscription restoration)
     */
    SUBSCRIPTION_RESTARTED(7),
    /**
     * A subscription price change has successfully been confirmed by the user.
     */
    SUBSCRIPTION_PRICE_CHANGE_CONFIRMED(8),
    /**
     *  A subscription's recurrence time has been extended.
     */
    SUBSCRIPTION_DEFERRED(9),
    /**
     * A subscription has been paused.
     */
    SUBSCRIPTION_PAUSED(10),
    /**
     *  A subscription pause schedule has been changed.
     */
    SUBSCRIPTION_PAUSE_SCHEDULE_CHANGED(11),
    /**
     * A subscription has been revoked from the user before the expiration time.
     */
    SUBSCRIPTION_REVOKED(12),
    /**
     * A subscription has expired.
     */
    SUBSCRIPTION_EXPIRED(13);


    companion object {
        private val codes = values().associateBy(GooglePlayNotificationType::code)
        @JvmStatic
        @JsonCreator
        fun from(value: Int) = codes[value]
    }
}