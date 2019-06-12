package com.dietmap.yaak.api.purchase

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/purchase")
class PurchaseController {

    @GetMapping
    fun hello() = "PurchaseController get mapping"
}