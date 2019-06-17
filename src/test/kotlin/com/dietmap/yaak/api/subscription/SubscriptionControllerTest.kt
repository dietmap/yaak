package com.dietmap.yaak.api.subscription

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
            "cancellationDateMs", "webOrderLineItemId", "latestReceipt" , "latestReceiptInfo",
            "latestExpiredReceipt", "latestExpiredReceiptInfo", true, "autoRenewProductId",
            "autoRenewStatusChangeDate", "autoRenewStatusChangeDatePst", "autoRenewStatusChangeDateMs")

    override fun setup() {
    }

    @Test
    fun `simulate subscription status update notification`() {
        this.mockMvc.perform(
                MockMvcRequestBuilders.post("/api/subscription/statusUpdateNotification")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(asJsonString(testStatusUpdateNotification))
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk)
    }

}