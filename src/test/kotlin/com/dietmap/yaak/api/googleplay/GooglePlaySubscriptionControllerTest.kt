package com.dietmap.yaak.api.googleplay

import com.dietmap.yaak.SupportController
import com.dietmap.yaak.domain.googleplay.AndroidPublisherClientConfiguration
import com.dietmap.yaak.domain.googleplay.GooglePlaySubscriptionService
import com.nimbusds.jose.util.Base64
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.*
import org.mockito.Mockito
import org.mockito.Mockito.doThrow
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.web.server.ResponseStatusException

@TestPropertySource(properties = ["yaak.google-play.enabled = true"])
internal class GooglePlaySubscriptionControllerTest : SupportController() {
    @MockBean
    lateinit var config: AndroidPublisherClientConfiguration
    @MockBean
    lateinit var subscriptionService: GooglePlaySubscriptionService

    @Test
    fun `should handle PubSub requests`() {
        val testNotification = PubSubDeveloperNotification(
                version = "1.0",
                packageName = "app.package.name",
                eventTimeMillis = 1574940416735,
                testNotification = GooglePlayTestNotification("1.0"),
                subscriptionNotification = null
        )
        val base64Data = Base64.encode(asJsonString(testNotification))
        val request = PubSubRequest("app.subscription", PubSubMessage("message.id", base64Data.toString()))

        mockMvc.perform(
                post("/public/api/googleplay/subscriptions/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk)
    }

    @Test
    fun `should gracefully handle user app client errors`() {
        val request = PurchaseRequest(
                packageName = "app.package",
                subscriptionId = "app.subscription.id",
                purchaseToken = "purchase.token"
        )
        `when`(subscriptionService.handlePurchase(anyString(), anyString(), anyString(), anyBoolean())).thenThrow(ResponseStatusException(HttpStatus.BAD_REQUEST, "Error communicating with user app"))

        mockMvc.perform(
                post("/api/googleplay/subscriptions/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest)
    }
}