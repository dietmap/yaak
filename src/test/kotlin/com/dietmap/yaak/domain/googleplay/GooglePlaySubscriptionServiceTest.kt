package com.dietmap.yaak.domain.googleplay

import com.dietmap.yaak.domain.googleplay.GooglePlaySubscriptionService
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@Disabled("To be run manually")
@SpringBootTest
internal class GooglePlaySubscriptionServiceTest {
    @Autowired
    lateinit var service: GooglePlaySubscriptionService


    @Test
    fun handlePurchase() {
        val purchase = service.handlePurchase(
                "net.app.staging.selfsigned",
                "app.billing.month.1",
                "dkcphkndppjfgombblmYr6a8T9fh_aT93YKvPaem3r-_cRpaI-v0NfQVJrHPQAd2XeMrwo59KQuQ"
        )
        print(purchase)
    }
}