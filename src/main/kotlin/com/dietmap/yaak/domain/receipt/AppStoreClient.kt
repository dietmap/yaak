package com.dietmap.yaak.domain.receipt

import com.dietmap.yaak.api.receipt.ReceiptRequest
import com.dietmap.yaak.api.receipt.ReceiptResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AppStoreClient {

    private val restTemplate: RestTemplate
    private val verifyReceiptUrl: String

    constructor(restTemplateBuilder: RestTemplateBuilder, @Value("\${appstore.base-url}") verifyReceiptUrlIn: String) {
        restTemplate = restTemplateBuilder.rootUri(verifyReceiptUrlIn).build()
        verifyReceiptUrl = verifyReceiptUrlIn
    }

    fun verifyReceipt(receiptRequest : ReceiptRequest): ReceiptResponse {
        return restTemplate.postForObject("/verifyReceipt", receiptRequest, ReceiptResponse::class.java)!!
    }

    fun isVerified(receiptRequest : ReceiptRequest): Boolean {
        val receiptResponse : ReceiptResponse? =
                restTemplate.postForObject("/verifyReceipt", receiptRequest, ReceiptResponse::class.java)

        return receiptResponse?.status == 0
    }

}