package com.dietmap.yaak.api.appstore.subscription

import com.dietmap.yaak.SupportController
import com.dietmap.yaak.domain.userapp.UserAppClient
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

internal class SubscriptionControllerTest : SupportController() {

    @MockBean
    private lateinit var userAppClient: UserAppClient

    private val testStatusUpdateNotification: StatusUpdateNotification = StatusUpdateNotification(
            "sandbox", NotificationStatus.CANCEL, "password", "cancellationDate", "cancellationDatePst",
            "cancellationDateMs", "webOrderLineItemId", "latestReceipt", "latestReceiptInfo",
            "latestExpiredReceipt", "latestExpiredReceiptInfo", true, "autoRenewProductId",
            "autoRenewStatusChangeDate", "autoRenewStatusChangeDatePst", "autoRenewStatusChangeDateMs")

    @Test
    fun `simulate subscription status update notification`() {
        this.mockMvc.perform(
                MockMvcRequestBuilders.post("/api/appstore/subscriptions/statusUpdateNotification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(testStatusUpdateNotification))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk)
    }

}