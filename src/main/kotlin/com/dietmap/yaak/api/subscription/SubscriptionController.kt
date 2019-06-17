package com.dietmap.yaak.api.subscription

import com.dietmap.yaak.domain.receipt.UserAppClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api/subscription")
class SubscriptionController(private val userAppClient: UserAppClient) {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/statusUpdateNotification")
    fun statusUpdateNotification(@Valid @RequestBody statusUpdateNotification: StatusUpdateNotification): ResponseEntity<Unit> {

        logger.debug("StatusUpdateNotification {}", statusUpdateNotification)

        userAppClient.handleSubscriptionUpdate(statusUpdateNotification)

        return ResponseEntity.ok().build()
    }

}