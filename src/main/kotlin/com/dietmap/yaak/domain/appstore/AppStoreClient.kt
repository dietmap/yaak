package com.dietmap.yaak.domain.appstore

import com.dietmap.yaak.api.receipt.ReceiptRequest
import com.dietmap.yaak.api.receipt.ReceiptResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.*

@Component
class AppStoreClient {

    private val restTemplate: RestTemplate
    private val verifyReceiptUrl: String

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    constructor(restTemplateBuilder: RestTemplateBuilder, @Value("\${yaak.appstore-base-url}") verifyReceiptUrlIn: String) {
        restTemplate = restTemplateBuilder.rootUri(verifyReceiptUrlIn).build()
        verifyReceiptUrl = verifyReceiptUrlIn

        val converter = MappingJackson2HttpMessageConverter()
        converter.supportedMediaTypes = Arrays.asList(
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_OCTET_STREAM)
        restTemplate.messageConverters.add(converter)
    }

    @Retryable(value = [RuntimeException::class], maxAttempts = 3, backoff = Backoff(delay = 2000))
    fun verifyReceipt(receiptRequest : ReceiptRequest): ReceiptResponse {
        return processRequest(receiptRequest)
    }

    @Retryable(value = [RuntimeException::class], maxAttempts = 3, backoff = Backoff(delay = 2000))
    fun isVerified(receiptRequest : ReceiptRequest): Boolean {
        return processRequest(receiptRequest).status == 0
    }

    private fun processRequest(receiptRequest: ReceiptRequest): ReceiptResponse {
        logger.debug("Processing ReceiptRequest {}", receiptRequest)

        val receiptResponse: ReceiptResponse =
                restTemplate.postForObject("/verifyReceipt", prepareHttpHeaders(receiptRequest), ReceiptResponse::class.java)!!

        logger.debug("Getting ReceiptResponse {}", receiptResponse)

        if (receiptResponse.shouldRetry()) {
            logger.warn("Retrying due to ${receiptResponse.responseStatusCode} status code")
            throw RuntimeException("Processing failed with ${receiptResponse.responseStatusCode}")
        }
        return receiptResponse
    }

    private fun prepareHttpHeaders(receiptRequest: ReceiptRequest): HttpEntity<ReceiptRequest> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(receiptRequest, headers)
    }

}