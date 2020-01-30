package com.dietmap.yaak.api.appstore.subscription

import com.dietmap.yaak.SupportController
import com.dietmap.yaak.api.appstore.receipt.LatestReceiptInfo
import com.dietmap.yaak.domain.userapp.UserAppClient
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@Disabled("To be refactored")
internal class SubscriptionControllerTest : SupportController() {

    @MockBean
    private lateinit var userAppClient: UserAppClient

    @Mock
    private lateinit var latestReceiptInfo: LatestReceiptInfo

    @Mock
    private lateinit var unifiedReceipt: UnifiedReceipt


    private val testStatusUpdateNotification: StatusUpdateNotification = StatusUpdateNotification(
            "sandbox", AppStoreNotificationType.CANCEL, "cancellationDate", latestReceiptInfo, "",
            "", "expirationIntent", "latestExpiredReceipt", true, "", "autoRenewProductId",
            "autoRenewStatusChangeDate", 12323230, unifiedReceipt)

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