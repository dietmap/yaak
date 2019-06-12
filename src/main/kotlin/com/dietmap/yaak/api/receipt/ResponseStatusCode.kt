package com.dietmap.yaak.api.receipt

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class ResponseStatusCode(private val code: Int,
                              private val description: String) : Serializable {

    CODE_21000(21000, "The request to the App Store was not made using the HTTP POST request method"),
    CODE_21001(21001, "This status code is no longer sent by the App Store"),
    CODE_21002(21002, "The data in the receipt-data property was malformed or missing"),
    CODE_21003(21003, "The receipt could not be authenticated"),
    CODE_21004(21004, "The shared secret you provided does not match the shared secret on file for your account"),
    CODE_21005(21005, "The receipt server is not currently available"),
    CODE_21006(21006, "This receipt is valid but the subscription has expired. When this status code is returned to your server,the receipt data is also decoded and returned as part of the response"),
    CODE_21007(21007, "This receipt is from the test environment, but it was sent to the production environment for verification"),
    CODE_21008(21008, "This receipt is from the production environment, but it was sent to the test environment for verification"),
    CODE_21010(21010, "Internal data access error. Try again later"),
    CODE_21100(21100, "The user account cannot be found or has been deleted");

    companion object {
        @JvmStatic
        fun getByCode(code: Int) : ResponseStatusCode? = values().firstOrNull { e -> e.code == code }
    }

    override fun toString(): String {
        return "(code=$code, description='$description')"
    }

    @JsonProperty
    fun getDescription() : String = description

    @JsonProperty
    fun getCode() : Int = code
}