package com.dietmap.yaak.domain.receipt

import com.dietmap.yaak.api.receipt.ReceiptResponse
import com.dietmap.yaak.api.subscription.StatusUpdateNotification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class UserAppClient {

    private val restTemplate: RestTemplate
    private val handleReceiptUpdateUrl: String
    private val handleSubscriptionUpdateUrl: String

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    constructor(restTemplateBuilder: RestTemplateBuilder,
                @Value("\${userapp.handle-receipt-update-url}") handleReceiptUpdateUrl: String,
                @Value("\${userapp.handle-subscription-update-url}") handleSubscriptionUpdateUrl: String) {
        restTemplate = restTemplateBuilder.build()
        this.handleReceiptUpdateUrl = handleReceiptUpdateUrl
        this.handleSubscriptionUpdateUrl = handleSubscriptionUpdateUrl
    }

    fun handleReceiptUpdate(receiptResponse: ReceiptResponse) {
        logger.debug("Processing ReceiptResponse {}", receiptResponse)

        restTemplate.postForLocation(handleReceiptUpdateUrl, receiptResponse)
    }

    fun handleSubscriptionUpdate(statusUpdateNotification: StatusUpdateNotification) {
        logger.debug("Processing StatusUpdateNotification {}", statusUpdateNotification)

        restTemplate.postForLocation(handleSubscriptionUpdateUrl, statusUpdateNotification)
    }

}