package com.dietmap.yaak.api.appstore.receipt

import com.dietmap.yaak.SupportController
import com.dietmap.yaak.domain.appstore.AppStoreClient
import com.dietmap.yaak.domain.userapp.UserAppClient
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@Disabled("To be refactored")
internal class ReceiptControllerTest : SupportController() {

    @MockBean
    private lateinit var appStoreClient: AppStoreClient

    @MockBean
    private lateinit var userAppClient: UserAppClient

    @Mock
    private lateinit var latestReceiptInfo: LatestReceiptInfo

    @Mock
    private lateinit var receipt: Receipt

    @Mock
    private lateinit var pendingRenewalInfo: PendingRenewalInfo

    private val testResponseStatusOk: ReceiptResponse = ReceiptResponse(
            0, "sandbox", receipt, "latestReceipt", latestReceiptInfo,
            pendingRenewalInfo , false )

    private val testResponseStatusError: ReceiptResponse = ReceiptResponse(
            21010, "sandbox", receipt, null, latestReceiptInfo,
            pendingRenewalInfo , true )

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
                post("/api/appstore/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(testRequestOk))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").isNumber)
                .andExpect(jsonPath("$.receipt").isString)
                .andExpect(jsonPath("$.is-retryable").isBoolean)
                .andExpect(jsonPath("$.status", equalTo(testResponseStatusOk.status)))
                .andExpect(jsonPath("$.latest_receipt", notNullValue()))
    }

    @Test
    fun `should return receipt response with status info for a valid request`() {
        this.mockMvc.perform(
                post("api/appstore/receipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(testRequestError))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").isNumber)
                .andExpect(jsonPath("$.is-retryable").isBoolean)
                .andExpect(jsonPath("$.status", equalTo(testResponseStatusError.status)))
                .andExpect(jsonPath("$.status_info", notNullValue()))
    }
}