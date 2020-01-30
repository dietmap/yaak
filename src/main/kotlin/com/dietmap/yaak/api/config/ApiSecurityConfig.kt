package com.dietmap.yaak.api.config

import com.dietmap.yaak.api.config.YaakSecurityType.*
import mu.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest


@Configuration
@EnableWebSecurity
@Order(1)
class ApiSecurityConfig(val securityProperties: YaakSecurityProperties) : WebSecurityConfigurerAdapter() {

    private val logger = KotlinLogging.logger { }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/api/appstore/subscriptions/statusUpdateNotification");
    }

    @Throws(Exception::class)
    override fun configure(httpSecurity: HttpSecurity) {
        val securityConfigurer = httpSecurity
                .antMatcher("/api/**")
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        logger.info { "YAAK security type is: ${securityProperties.type}" }
         when(securityProperties.type) {
             API_KEY -> securityConfigurer
                     .and()
                     .addFilter(securityProperties.apiKeyAuthFilter())
                     .authorizeRequests()
                     .anyRequest()
                     .authenticated()
             OAUTH -> securityConfigurer
                     .and()
                     .authorizeRequests()
                     .anyRequest()
                     .authenticated()
                     .and()
                     .oauth2ResourceServer().jwt()
             NONE -> logger.info { "Communication with YAAK is not secured, ${API_KEY} or ${OAUTH} YAAK security is recommended" }
         }
    }
}

@Component
@ConfigurationProperties(prefix = "yaak.security")
class YaakSecurityProperties {
    lateinit var type: YaakSecurityType
    lateinit var apiKey: String

    fun apiKeyAuthFilter(): ApiKeyAuthFilter {
        val filter = ApiKeyAuthFilter()

        filter.setAuthenticationManager { authentication ->
            val principal = authentication.principal as String

            if (apiKey != principal) {
                throw BadCredentialsException("The API key was not found or is not the expected value.")
            }

            authentication.isAuthenticated = true
            authentication
        }
        return filter
    }
}

enum class YaakSecurityType {
    API_KEY, OAUTH, NONE
}

class ApiKeyAuthFilter : AbstractPreAuthenticatedProcessingFilter() {

    override fun getPreAuthenticatedPrincipal(request: HttpServletRequest): Any {
        return request.getHeader(ApiCommons.API_KEY_HEADER)
    }

    override fun getPreAuthenticatedCredentials(request: HttpServletRequest): Any {
        return "N/A"
    }

}