package com.dietmap.yaak.domain.userapp

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal

@SpringBootTest
@Disabled("To be run manually")
internal class UserAppClientTest {
    @Autowired
    lateinit var client: UserAppClient

    @Test
    fun sendSubscriptionNotification() {
        val notificationResponse = client.sendSubscriptionNotification(UserAppSubscriptionNotification(
                notificationType = NotificationType.INITIAL_BUY,
                description = "Yaak integration test call",
                currencyCode = "PLN",
                countryCode = "PL",
                price = BigDecimal.TEN,
                transactionId = "testTransactionID",
                appMarketplace = "GooglePlay",
                productId = "oneMonthSubscription",
                orderingUserInternalId = 1
        ))
        print(notificationResponse)
    }
}