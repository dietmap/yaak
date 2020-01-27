package com.dietmap.yaak.api.appstore.subscription

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AppStoreNotificationTypeTest {

    @Test
    fun `should create proper notification type by its name`() {
        val interactiveRenewalNotification = AppStoreNotificationType.fromName("INTERACTIVE_RENEWAL")

        assertEquals(interactiveRenewalNotification?.getCode(), 7);
    }

    @Test
    fun `should create proper notification type by its code`() {
        val interactiveRenewalNotification = AppStoreNotificationType.fromCode(7)

        assertEquals(interactiveRenewalNotification?.name, "INTERACTIVE_RENEWAL");
    }
}