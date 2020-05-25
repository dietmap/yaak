package com.dietmap.yaak.api.subscription

import com.dietmap.yaak.SupportController
import com.dietmap.yaak.domain.googleplay.GooglePlaySubscriptionService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

internal class SubscriptionControllerTest : SupportController() {

    @MockBean
    lateinit var subscriptionService: GooglePlaySubscriptionService

    @Test
    fun `should cancel subscription in Play Store`() {
        val request = SubscriptionCancelRequest(
                packageName = "app.package",
                subscriptionId = "app.subscription.id",
                purchaseToken = "purchase.token")

        mockMvc.perform(
                post("/subscriptions/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(asJsonString(request))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk)

        verify(subscriptionService).cancelPurchase(request)
    }

}