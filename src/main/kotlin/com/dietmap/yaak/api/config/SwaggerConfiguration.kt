package com.dietmap.yaak.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.*
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.function.Predicate

@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration::class)
class SwaggerConfiguration {

    @Bean
    fun adminApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .apiInfo(ApiInfoBuilder().title("Yaak API documentation").build())
                .enableUrlTemplating(true)
                .select()
                    .apis(Predicate.not(RequestHandlerSelectors.basePackage("org.springframework.boot")))
                    .apis(Predicate.not(RequestHandlerSelectors.basePackage("org.springframework.cloud")))
                    .apis(Predicate.not(RequestHandlerSelectors.basePackage("org.springframework.data.rest.webmvc")))
                .build();
    }

    @Bean
    internal fun uiConfig(): UiConfiguration {
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .displayOperationId(false)
                .defaultModelsExpandDepth(1)
                .defaultModelExpandDepth(1)
                .defaultModelRendering(ModelRendering.EXAMPLE)
                .displayRequestDuration(true)
                .docExpansion(DocExpansion.NONE)
                .filter(false)
                .maxDisplayedTags(null)
                .operationsSorter(OperationsSorter.ALPHA)
                .showExtensions(false)
                .tagsSorter(TagsSorter.ALPHA)
                .validatorUrl(null)
                .build()
    }

}
