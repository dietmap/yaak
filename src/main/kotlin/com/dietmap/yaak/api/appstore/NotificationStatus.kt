package com.dietmap.yaak.api.appstore

import com.fasterxml.jackson.annotation.JsonProperty

enum class NotificationStatus (private val description: String) {

    INITIAL_BUY ("Occurs at the initial purchase of the subscription. Store the latest_receipt on your server " +
            "as a token to verify the user’s subscription status at any time, by validating it with the App Store."),
    CANCEL ("Indicates that the subscription was canceled either by Apple customer support or by the " +
            "App Store when the user upgraded their subscription. " +
            "The cancellation_date key contains the date and time when the subscription was canceled or upgraded."),
    RENEWAL ("Indicates successful automatic renewal of an expired subscription that failed to renew in the past. " +
            "Check expires_date to determine the next renewal date and time."),
    INTERACTIVE_RENEWAL ("Indicates the customer renewed a subscription interactively, " +
            "either by using your app’s interface, or on the App Store in account settings. Make service available immediately."),
    DID_CHANGE_RENEWAL_PREF ("Indicates the customer made a change in their subscription plan that " +
            "takes effect at the next renewal. The currently active plan is not affected."),
    DID_CHANGE_RENEWAL_STATUS ("Indicates a change in the subscription renewal status. Check the " +
            "auto_renew_status_change_date_ms and the auto_renew_status in the JSON to know the date and time " +
            "when the status was last updated and the current renewal status.");

    @JsonProperty
    fun getDescription() : String = description

    override fun toString(): String {
        return "NotificationStatus(description='$description')"
    }

}