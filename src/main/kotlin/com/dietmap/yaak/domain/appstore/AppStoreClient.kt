package com.dietmap.yaak.domain.appstore

import com.dietmap.yaak.api.appstore.receipt.ReceiptRequest
import com.dietmap.yaak.api.appstore.receipt.ReceiptResponse
import com.dietmap.yaak.api.appstore.receipt.ReceiptResponseStatus
import com.dietmap.yaak.api.appstore.receipt.ResponseStatusCode
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.Duration


@Component
@ConditionalOnProperty("yaak.app-store.enabled", havingValue = "true")
class AppStoreClient {

    private val productionRestTemplate: RestTemplate
    private val sandboxRestTemplate: RestTemplate
    private val password: String

    private val logger = KotlinLogging.logger { }

    constructor(restTemplateBuilder: RestTemplateBuilder,
                @Value("\${yaak.app-store.production-url}") productionUrl: String,
                @Value("\${yaak.app-store.sandbox-url}") sandboxUrl: String,
                @Value("\${yaak.app-store.password}") passwordIn: String) {

        productionRestTemplate = restTemplateBuilder.rootUri(productionUrl)
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build()

        sandboxRestTemplate = restTemplateBuilder.rootUri(sandboxUrl)
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build()

        password = passwordIn

        val converter = MappingJackson2HttpMessageConverter()
        converter.supportedMediaTypes = listOf(
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_OCTET_STREAM)

        productionRestTemplate.messageConverters.add(converter)
        sandboxRestTemplate.messageConverters.add(converter)
    }

    @Retryable(value = [RuntimeException::class], maxAttempts = 3, backoff = Backoff(delay = 3000))
    fun verifyReceipt(receiptRequest: ReceiptRequest): ReceiptResponse {
        return processRequest(receiptRequest)
    }

    @Retryable(value = [RuntimeException::class], maxAttempts = 3, backoff = Backoff(delay = 3000))
    fun isVerified(receiptRequest: ReceiptRequest): Boolean {
        return processRequest(receiptRequest).status == 0
    }

    @Recover
    fun recoverVerifyReceipt(runtimeException: RuntimeException, receiptRequest: ReceiptRequest) : ReceiptResponse {

        logger.debug { "recoverVerifyReceipt: ReceiptRequest $receiptRequest for exception $runtimeException" }

        val receiptResponseStatus: ReceiptResponseStatus =
                productionRestTemplate.postForObject("/verifyReceipt", prepareHttpHeaders(receiptRequest), ReceiptResponseStatus::class.java)!!

        logger.debug { "recoverVerifyReceipt: ReceiptResponseStatus $receiptResponseStatus" }

        if (receiptResponseStatus.responseStatusCode!! == ResponseStatusCode.CODE_21007) {
            return sandboxRestTemplate.postForObject("/verifyReceipt", prepareHttpHeaders(receiptRequest), ReceiptResponse::class.java)!!
        } else {
            val message = "Cannot process ReceiptRequest due to exception $runtimeException";
            logger.error { message }
            throw ReceiptValidationException(message)
        }
    }

    private fun processRequest(receiptRequest: ReceiptRequest): ReceiptResponse {
        receiptRequest.password = password

        logger.debug { "processRequest: ReceiptRequest $receiptRequest" }

        val receiptResponse: ReceiptResponse =
                productionRestTemplate.postForObject("/verifyReceipt", prepareHttpHeaders(receiptRequest), ReceiptResponse::class.java)!!

        logger.debug { "processRequest: ReceiptResponse $receiptResponse" }

        if (receiptResponse.shouldRetry()) {
            val message = "Retrying due to ${receiptResponse.responseStatusCode} status code"
            logger.warn { message }
            throw RetryableException(message)
        }
        return receiptResponse
    }

    private fun prepareHttpHeaders(receiptRequest: ReceiptRequest): HttpEntity<ReceiptRequest> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(receiptRequest, headers)
    }

}