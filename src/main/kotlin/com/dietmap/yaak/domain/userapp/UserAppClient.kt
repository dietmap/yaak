package com.dietmap.yaak.domain.userapp

import com.dietmap.yaak.api.config.YaakSecurityProperties
import com.dietmap.yaak.api.config.YaakSecurityType
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.reactive.function.client.*
import reactor.core.publisher.Mono
import java.util.*
import java.util.function.Consumer


@Component
class UserAppClient(val webClient: WebClient, @Value("\${yaak.user-app.subscription-webhook-url}") handleSubscriptionUpdateUrl: String) {

    private val subscriptionNotificationUrl: String = handleSubscriptionUpdateUrl
    private val logger = KotlinLogging.logger { }

    fun sendSubscriptionNotification(notification: UserAppSubscriptionNotification): UserAppSubscriptionOrder? {
        logger.debug { "Processing UserAppSubscriptionNotification $notification" }
        return webClient.post()
                .uri(subscriptionNotificationUrl)
                .bodyValue(notification)
                //TODO get rid of fixed authorization type
                .attributes(clientRegistrationId("user-app-client"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(UserAppSubscriptionOrder::class.java)
                .blockFirst()
    }
}

@Configuration
class UserAppClientConfiguration {
    @Value("\${spring.security.oauth2.client.registration.user-app-client-password.username:''}")
    lateinit var username: String;
    @Value("\${spring.security.oauth2.client.registration.user-app-client-password.password:''}")
    lateinit var password: String;


    @Bean
    @ConditionalOnProperty("yaak.security.type", havingValue = "OAUTH")
    @Primary
    fun oauth2AuthorizedWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, securityProperties: YaakSecurityProperties): WebClient {
        val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .apply(if (securityProperties.type == YaakSecurityType.OAUTH) TokenRelayingFilterFunction(oauth2Client).oauth2Configuration() else oauth2Client.oauth2Configuration())
                .build()
    }

    @Bean
    @ConditionalOnMissingBean(WebClient::class)
    fun unauthorizedWebClient(): WebClient {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()
    }

    class TokenRelayingFilterFunction(val oauth2FilterFunction: ServletOAuth2AuthorizedClientExchangeFilterFunction) : ExchangeFilterFunction {
        private val logger = KotlinLogging.logger { }

        override fun filter(clientRequest: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
            if (RequestContextHolder.getRequestAttributes() != null) {
                val attr = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
                val request = attr.request
                val accessToken = request.getHeader(HttpHeaders.AUTHORIZATION)
                if (StringUtils.hasText(accessToken)) {
                    val authorizedClientRequest = ClientRequest.from(clientRequest)
                            .headers { headers: HttpHeaders -> headers.set(HttpHeaders.AUTHORIZATION, accessToken) }
                            .build()
                    logger.debug { "Relaying access token to user app" }
                    return Mono.defer { next.exchange(authorizedClientRequest) }
                }
            }
            return oauth2FilterFunction.filter(clientRequest, next)
        }

        fun oauth2Configuration(): Consumer<WebClient.Builder> {
            return Consumer { builder: WebClient.Builder -> builder.defaultRequest(oauth2FilterFunction.defaultRequest()).filter(this) }
        }
    }

    private val usernamePasswordAttributesMapper: (OAuth2AuthorizeRequest) -> Map<String, Any> by lazy {
        { _: OAuth2AuthorizeRequest ->
            var contextAttributes: Map<String, Any> = Collections.emptyMap()
            if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
                contextAttributes = HashMap()

                // `PasswordOAuth2AuthorizedClientProvider` requires both attributes
                contextAttributes.put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, username)
                contextAttributes.put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password)
            }
            contextAttributes
        }
    }

    @Bean
    @ConditionalOnProperty("yaak.security.type", havingValue = "OAUTH")
    fun oauth2AuthorizedClientManager(clientRegistrationRepository: ClientRegistrationRepository,
                                      authorizedClientRepository: OAuth2AuthorizedClientRepository): OAuth2AuthorizedClientManager {
        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .refreshToken()
                .clientCredentials()
                .password()
                .build()
        val authorizedClientManager = DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository)
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        // For the `password` grant, the `username` and `password` are supplied via request parameters,
        // so map it to `OAuth2AuthorizationContext.getAttributes()`.
        authorizedClientManager.setContextAttributesMapper(usernamePasswordAttributesMapper)
        return authorizedClientManager
    }

}