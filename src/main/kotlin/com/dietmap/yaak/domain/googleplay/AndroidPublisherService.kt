package com.dietmap.yaak.domain.googleplay

import com.dietmap.yaak.domain.googleplay.AndroidPublisherClientConfiguration.Companion.DEFAULT_TENANT
import com.google.api.services.androidpublisher.AndroidPublisher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@ConditionalOnProperty("yaak.google-play.enabled", havingValue = "true")
@Service
class AndroidPublisherService(private val androidPublishers: Map<String, AndroidPublisher>) {

    fun tenant(tenant: String?) = androidPublishers.getOrDefault(tenant?.toUpperCase() ?: DEFAULT_TENANT, androidPublishers[DEFAULT_TENANT])!!

}