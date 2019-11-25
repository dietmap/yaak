package com.dietmap.yaak.api.appstore.receipt

import com.dietmap.yaak.domain.appstore.AppStoreClient
import com.dietmap.yaak.domain.userapp.NotificationType
import com.dietmap.yaak.domain.userapp.UserAppClient
import com.dietmap.yaak.domain.userapp.UserAppSubscriptionNotification
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import javax.validation.Valid

@RestController
@RequestMapping("/api/appstore/receipt")
class ReceiptController(private val appStoreClient : AppStoreClient, private val userAppClient : UserAppClient) {

    @PostMapping
    fun verifyWithResponse(@RequestBody @Valid receiptRequest: ReceiptRequest) : ResponseEntity<ReceiptResponse> {
        val receiptResponse = appStoreClient.verifyReceipt(receiptRequest)

        return if (receiptResponse.isValid()) ResponseEntity.ok(receiptResponse) else ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(receiptResponse)
    }

    @PostMapping("/verify")
    fun verify(@RequestBody @Valid receiptRequest: ReceiptRequest) : ResponseEntity<Unit> {
        val receiptResponse = appStoreClient.verifyReceipt(receiptRequest)

        return if (appStoreClient.isVerified(receiptRequest)) {
            //TODO fill in with proper data and handle errors
            val notification = UserAppSubscriptionNotification(
                    notificationType = NotificationType.RENEWAL,
                    productId = "",
                    orderingUserInternalId = 1,
                    transactionId = "",
                    price = BigDecimal.ONE,
                    countryCode = "PL",
                    currencyCode = "PLN",
                    appMarketplace = "AppStore",
                    description = "Recipe update from AppStore"
            )
            userAppClient.sendSubscriptionNotification(notification)
            ResponseEntity.ok().build()
        }
        else ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
    }

}