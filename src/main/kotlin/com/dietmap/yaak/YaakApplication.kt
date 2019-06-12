package com.dietmap.yaak

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableRetry
class YaakApplication

fun main(args: Array<String>) {
	runApplication<YaakApplication>(*args)
}
