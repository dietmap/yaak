package com.dietmap.yaak.api.googleplay

import com.dietmap.yaak.SupportController
import com.dietmap.yaak.domain.googleplay.AndroidPublisherClientConfiguration
import com.dietmap.yaak.domain.googleplay.GooglePlaySubscriptionService
import com.nimbusds.jose.util.Base64
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.`when`
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
                purchaseToken = "purchase.token",
                orderingUserId = "1",
                discountCode = "234"
        )
        `when`(subscriptionService.handlePurchase(request)).thenThrow(ResponseStatusException(HttpStatus.BAD_REQUEST, "Error communicating with user app"))

        mockMvc.perform(
                post("/api/googleplay/subscriptions/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest)
    }

    @Test
    fun `should cancel subscription in Play Store`() {
        val request = SubscriptionCancelRequest(
                packageName = "app.package",
                subscriptionId = "app.subscription.id",
                purchaseToken = "purchase.token")

        mockMvc.perform(
                post("/api/googleplay/subscriptions/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk)

        Mockito.verify(subscriptionService).cancelPurchase(request)
    }

}