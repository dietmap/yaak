package com.dietmap.yaak.config

import com.dietmap.yaak.api.ApiCommons
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter

import javax.servlet.http.HttpServletRequest

class ApiKeyAuthFilter : AbstractPreAuthenticatedProcessingFilter() {

    override fun getPreAuthenticatedPrincipal(request: HttpServletRequest): Any {
        return request.getHeader(ApiCommons.API_KEY_HEADER)
    }

    override fun getPreAuthenticatedCredentials(request: HttpServletRequest): Any {
        return "N/A"
    }

}