package com.dietmap.yaak

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
abstract class SupportController {

    @Autowired
    lateinit var objectMapper: ObjectMapper
    @Autowired
    lateinit var context: WebApplicationContext
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun baseSetup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build()
        setup()
    }

    abstract fun setup()

    protected fun asJsonString(obj: Any): String {
        return objectMapper.writeValueAsString(obj)
    }
}