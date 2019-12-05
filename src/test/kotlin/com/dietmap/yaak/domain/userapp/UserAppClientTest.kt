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
                notificationType = NotificationType.SUBSCRIPTION_PURCHASED,
                description = "Yaak integration test call",
                productId = "oneMonthSubscription",
                countryCode = "PL",
                price = BigDecimal.TEN,
                currencyCode = "PLN",
                transactionId = "testTransactionID",
                appMarketplace = AppMarketplace.GOOGLE_PLAY,
                expiryTimeMillis = 1,
                orderingUserId = null
        ))
        print(notificationResponse)
    }
}