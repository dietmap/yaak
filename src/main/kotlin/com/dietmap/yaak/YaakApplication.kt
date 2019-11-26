package com.dietmap.yaak

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableConfigurationProperties
@EnableRetry
class YaakApplication

fun main(args: Array<String>) {
	runApplication<YaakApplication>(*args)
}
