package com.dietmap.yaak.domain.appstore

import com.dietmap.yaak.api.appstore.receipt.ReceiptRequest
import com.dietmap.yaak.api.appstore.receipt.ReceiptResponse
import com.dietmap.yaak.api.appstore.receipt.ReceiptResponseStatus
import com.dietmap.yaak.api.appstore.receipt.ResponseStatusCode
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.*
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Component
@ConstructorBinding
@ConfigurationProperties(prefix = "yaak")
class AppStoreClientProperties {
    var appstore: AppStoreProperties = AppStoreProperties.empty()
    var multitenant: Map<String, AppStoreClientProperties> = emptyMap()
}

@ConstructorBinding
class AppStoreProperties(
    val password: String? = null,
    val productionUrl: String? = null,
    val sandboxUrl: String? = null
) {
    companion object {
        fun empty() = AppStoreProperties()
    }
}

@Configuration
@ConditionalOnProperty("yaak.app-store.enabled", havingValue = "true")
class AppStoreClientConfiguration {

    companion object {
        const val DEFAULT_TENANT = "DEFAULT"
        const val TIMEOUT_IN_SECS = 5L
    }

    @Bean
    fun appStoreClients(properties: AppStoreClientProperties, builder: RestTemplateBuilder) =
        properties.multitenant
            .mapValues { t -> createAppStoreClient(builder, t.value.appstore, properties.appstore) }
            .plus(DEFAULT_TENANT to createAppStoreClient(builder, properties.appstore))

    private fun createAppStoreClient(
        builder: RestTemplateBuilder,
        tenantProperties: AppStoreProperties,
        defaults: AppStoreProperties = AppStoreProperties.empty()
    ): AppStoreClient {
        val converter = MappingJackson2HttpMessageConverter()
        converter.supportedMediaTypes = listOf(APPLICATION_JSON, APPLICATION_OCTET_STREAM)
        val productionTemplate = builder.rootUri((tenantProperties.productionUrl ?: defaults.productionUrl)!!)
            .setConnectTimeout(Duration.ofSeconds(TIMEOUT_IN_SECS))
            .setReadTimeout(Duration.ofSeconds(TIMEOUT_IN_SECS))
            .messageConverters(converter)
            .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .build()
        val sandboxTemplate = builder.rootUri((tenantProperties.sandboxUrl ?: defaults.sandboxUrl)!!)
            .setConnectTimeout(Duration.ofSeconds(TIMEOUT_IN_SECS))
            .setReadTimeout(Duration.ofSeconds(TIMEOUT_IN_SECS))
            .messageConverters(converter)
            .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .build()
        return AppStoreClient(productionTemplate, sandboxTemplate, (tenantProperties.password ?: defaults.password)!!)
    }

}

class AppStoreClient(private val productionTemplate: RestTemplate, private val sandboxTemplate: RestTemplate, private val password: String) {

    companion object {
        private val logger = KotlinLogging.logger { }
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
    fun recoverVerifyReceipt(runtimeException: RuntimeException, receiptRequest: ReceiptRequest): ReceiptResponse {
        logger.debug { "recoverVerifyReceipt: ReceiptRequest $receiptRequest for exception $runtimeException" }
        val receiptResponseStatus = productionTemplate.postForObject("/verifyReceipt", receiptRequest, ReceiptResponseStatus::class.java)!!
        logger.debug { "recoverVerifyReceipt: ReceiptResponseStatus $receiptResponseStatus" }
        if (receiptResponseStatus.responseStatusCode!! == ResponseStatusCode.CODE_21007) {
            return sandboxTemplate.postForObject("/verifyReceipt", receiptRequest, ReceiptResponse::class.java)!!
        } else {
            val message = "Cannot process ReceiptRequest due to exception $runtimeException"
            logger.error { message }
            throw ReceiptValidationException(message)
        }
    }

    private fun processRequest(receiptRequest: ReceiptRequest): ReceiptResponse {
        receiptRequest.password = password
        logger.debug { "processRequest: ReceiptRequest $receiptRequest" }
        var receiptResponse = productionTemplate.postForObject("/verifyReceipt", receiptRequest, ReceiptResponse::class.java)!!
        if (receiptResponse.responseStatusCode!! == ResponseStatusCode.CODE_21007) {
            receiptResponse = sandboxTemplate.postForObject("/verifyReceipt", receiptRequest, ReceiptResponse::class.java)!!
        }
        logger.debug { "processRequest: ReceiptResponse $receiptResponse" }
        if (receiptResponse.shouldRetry()) {
            val message = "Retrying due to ${receiptResponse.responseStatusCode} status code"
            logger.warn { message }
            throw RetryableException(message)
        }
        return receiptResponse
    }

}