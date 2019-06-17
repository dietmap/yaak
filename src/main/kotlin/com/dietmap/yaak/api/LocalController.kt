package com.dietmap.yaak.api

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping
class LocalController {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/handle")
    fun handleUpdateLocally(@RequestBody request: Any) {
        logger.warn("Handling request locally. Change the configuration to point to your real API endpoints!")
    }
}