package com.dietmap.yaak.api.receipt

import com.dietmap.yaak.SupportController
import com.dietmap.yaak.domain.receipt.AppStoreClient
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


internal class ReceiptControllerTest : SupportController() {

    @MockBean
    private lateinit var appStoreClient: AppStoreClient

    private val testResponseStatusOk: ReceiptResponse = ReceiptResponse(
            0, "sandbox", "receipt", "latestReceipt", "latestReceiptInfo",
            "latestExpiredReceiptInfo" , "pendingRenewalInfo" , true )

    private val testResponseStatusError: ReceiptResponse = ReceiptResponse(
            21000, "sandbox", "receipt", "latestReceipt", "latestReceiptInfo",
            "latestExpiredReceiptInfo" , "pendingRenewalInfo" , true )

    private val testRequestOk: ReceiptRequest = ReceiptRequest(
            "receiptData","password",true)

    private val testRequestError: ReceiptRequest = ReceiptRequest(
            "receiptData","password")

    override fun setup() {
        `when`(appStoreClient.verifyReceipt(testRequestOk)).thenReturn(testResponseStatusOk)
        `when`(appStoreClient.verifyReceipt(testRequestError)).thenReturn(testResponseStatusError)
        `when`(appStoreClient.isVerified(testRequestOk)).thenReturn(true)
        `when`(appStoreClient.isVerified(testRequestError)).thenReturn(false)
    }

    @Test
    fun `should return receipt response for a valid request`() {
        this.mockMvc.perform(
                post("/api/receipt")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(asJsonString(testRequestOk))
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status").isNumber)
                .andExpect(jsonPath("$.receipt").isString)
                .andExpect(jsonPath("$.retryable").isBoolean)
                .andExpect(jsonPath("$.status", equalTo(testResponseStatusOk.status)))
                .andExpect(jsonPath("$.latest_receipt", notNullValue()))
    }

    @Test
    fun `should return receipt response with status info for a valid request`() {
        this.mockMvc.perform(
                post("/api/receipt")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(asJsonString(testRequestError))
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status").isNumber)
                .andExpect(jsonPath("$.receipt").isString)
                .andExpect(jsonPath("$.retryable").isBoolean)
                .andExpect(jsonPath("$.status", equalTo(testResponseStatusError.status)))
                .andExpect(jsonPath("$.latest_receipt", notNullValue()))
                .andExpect(jsonPath("$.status_info", notNullValue()))
    }

    @Test
    fun `should successfully verify receipt for a valid request`() {
        this.mockMvc.perform(
                post("/api/receipt/verify")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(asJsonString(testRequestOk))
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isOk)
    }

    @Test
    fun `should unsuccessfully verify receipt for a valid request`() {
        this.mockMvc.perform(
                post("/api/receipt/verify")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(asJsonString(testRequestError))
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isIAmATeapot)
    }
}