package com.dietmap.yaak.domain.appstore

import com.dietmap.yaak.api.appstore.receipt.ReceiptRequest
import com.dietmap.yaak.api.appstore.receipt.ReceiptResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.Duration


@Component
@ConditionalOnProperty("yaak.app-store.enabled", havingValue = "true")
class AppStoreClient {

    private val restTemplate: RestTemplate
    private val verifyReceiptUrl: String
    private val password: String

    private val logger = KotlinLogging.logger { }

    constructor(restTemplateBuilder: RestTemplateBuilder,
                @Value("\${yaak.app-store.base-url}") verifyReceiptUrlIn: String,
                @Value("\${yaak.app-store.password}") passwordIn: String) {
        restTemplate = restTemplateBuilder.rootUri(verifyReceiptUrlIn)
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build()
        verifyReceiptUrl = verifyReceiptUrlIn
        password = passwordIn

        val converter = MappingJackson2HttpMessageConverter()
        converter.supportedMediaTypes = listOf(
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_OCTET_STREAM)
        restTemplate.messageConverters.add(converter)
    }

    @Retryable(value = [RuntimeException::class], maxAttempts = 3, backoff = Backoff(delay = 3000))
    fun verifyReceipt(receiptRequest: ReceiptRequest): ReceiptResponse {
        return processRequest(receiptRequest)
    }

    @Retryable(value = [RuntimeException::class], maxAttempts = 3, backoff = Backoff(delay = 3000))
    fun isVerified(receiptRequest: ReceiptRequest): Boolean {
        return processRequest(receiptRequest).status == 0
    }

    private fun processRequest(receiptRequest: ReceiptRequest): ReceiptResponse {
        receiptRequest.password = password

        logger.debug { "Processing ReceiptRequest $receiptRequest" }

        val receiptResponse: ReceiptResponse =
                restTemplate.postForObject("/verifyReceipt", prepareHttpHeaders(receiptRequest), ReceiptResponse::class.java)!!

        logger.debug { "Getting ReceiptResponse $receiptResponse" }

        if (receiptResponse.shouldRetry()) {
            logger.warn { "Retrying due to ${receiptResponse.responseStatusCode} status code" }
            throw RuntimeException("Retrying due to ${receiptResponse.responseStatusCode}")
        }
        return receiptResponse
    }

    private fun prepareHttpHeaders(receiptRequest: ReceiptRequest): HttpEntity<ReceiptRequest> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(receiptRequest, headers)
    }

}