package com.dietmap.yaak.domain.userapp

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class UserAppSubscriptionNotificationTest {

    @Test
    fun `should create proper user subscription notification`() {
        val notification = UserAppSubscriptionNotification(
                notificationType = NotificationType.SUBSCRIPTION_PURCHASED,
                description = "Test",
                productId = "test-product-1",
                transactionId = "transId1",
                originalTransactionId = "orgTransId1",
                appMarketplace = AppMarketplace.APP_STORE,
                expiryTimeMillis = 12345,
                currencyCode = null,
                countryCode = null
        )

        assertNotNull(notification.notificationType)
        assertNotNull(notification.appMarketplace)
        assertNotNull(notification.price)
        assertNull(notification.currencyCode)
        assertNull(notification.countryCode)
    }
}