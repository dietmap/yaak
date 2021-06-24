package com.dietmap.yaak.domain.googleplay

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.Preconditions
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisherScopes
import com.google.common.base.Strings
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException
import java.util.*


@Component
@ConstructorBinding
@ConfigurationProperties(prefix = "yaak")
class GoogleDeveloperApiClientProperties {
    lateinit var googleplay: GooglePlayProperties
    var multitenant: List<MultitenantGooglePlayProperties> = emptyList()
}

@ConstructorBinding
class MultitenantGooglePlayProperties(
    val tenant: String,
    val googleplay: GooglePlayProperties
)

@ConstructorBinding
class GooglePlayProperties(
    val serviceAccountApiKeyBase64: String,
    val serviceAccountEmail: String,
    val applicationName: String
) {
    fun getServiceAccountApiKeyInputStream(): InputStream {
        val decoded = Base64.getDecoder().decode(serviceAccountApiKeyBase64)
        return ByteArrayInputStream(decoded)
    }
}

/**
 * Helper class to initialize the publisher APIs client library.
 *
 *
 * Before making any calls to the API through the client library you need to
 * call the [AndroidPublisherClientConfiguration.init] method. This will run
 * all precondition checks for for client id and secret setup properly in
 * resources/client_secrets.json and authorize this client against the API.
 *
 */
@ConditionalOnProperty("yaak.google-play.enabled", havingValue = "true")
@Configuration
class AndroidPublisherClientConfiguration(val properties: GoogleDeveloperApiClientProperties) {
    private val logger = KotlinLogging.logger { }

    /** Global instance of the JSON factory.  */
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()

    /** Global instance of the HTTP transport.  */
    private var HTTP_TRANSPORT: HttpTransport? = null

    companion object {
        const val DEFAULT_TENANT = "DEFAULT"
    }

    @Bean
    @Throws(IOException::class, GeneralSecurityException::class)
    fun androidPublishers() = properties.multitenant
        .associate { m -> m.tenant to createAndroidPublisher(m.googleplay) }
        .plus(DEFAULT_TENANT to createAndroidPublisher(properties.googleplay))

    private fun createAndroidPublisher(properties: GooglePlayProperties): AndroidPublisher {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(properties.applicationName), "applicationName cannot be null or empty!")
        newTrustedTransport()
        return AndroidPublisher
            .Builder(HTTP_TRANSPORT!!, JSON_FACTORY, authorizeWithServiceAccount(properties.serviceAccountEmail, properties))
            .setApplicationName(properties.applicationName)
            .build()
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun authorizeWithServiceAccount(serviceAccountEmail: String, properties: GooglePlayProperties): Credential {
        logger.info { "Authorizing using Service Account: $serviceAccountEmail" }
        // Build service account credential.
        return GoogleCredential.fromStream(properties.getServiceAccountApiKeyInputStream(), HTTP_TRANSPORT, JSON_FACTORY)
        .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER))
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun newTrustedTransport() {
        if (null == HTTP_TRANSPORT) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        }
    }
}