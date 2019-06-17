YAAK - Yet Another AppStore Kit
======

[![Build Status](https://travis-ci.com/dietmap/yaak.svg?branch=master)](https://travis-ci.com/dietmap/yaak)

### YAAK is a simple server that by its API makes In-App Purchase receipt and Auto-Renewable subscription validation easy

You should always validate receipts on the server, in [Apple's words](https://developer.apple.com/library/ios/releasenotes/General/ValidateAppStoreReceipt/Chapters/ValidateRemotely.html#//apple_ref/doc/uid/TP40010573-CH104-SW1):
> Use a trusted server to communicate with the App Store. Using your own server lets you design your app to recognize and trust only your server, and lets you ensure that your server connects with the App Store server. It is not possible to build a trusted connection between a user’s device and the App Store directly because you don’t control either end of that connection.

### Running YAAK locally

YAAK is a [Spring Boot](https://spring.io/projects/spring-boot) application written in [Kotlin](https://kotlinlang.org/) and built using [Gradle](https://gradle.org/). 
You can build a jar file and then run it on your local machine as follows:


```bash
$ git clone https://github.com/dietmap/yaak.git
$ cd yaak
$ ./gradlew clean build
$ java -jar build/libs/*.jar
```

Or you can run it from Gradle directly using the Spring Boot Gradle plugin. 

```bash
$ ./gradlew bootRun
```

Once you run it you can access YAAK here: http://localhost:8080/


### Configuration
There are two configuration files with corresponding profiles: [*sandbox*](./src/main/resources/application-sandbox.yml) 
and [*production*](./src/main/resources/application-production.yml)

By default YAAK runs on *sandbox* profile so if would like to changed it do it as follows:

```bash
export SPRING_PROFILES_ACTIVE=production
```

and then run

```bash
$ ./gradlew bootRun
```

### Plugin your application logic

There are two places where you can plugin your custom backend application logic.
Both of them are REST endpoints accepting HTTP POST method, passing original request as payload and not returning a response.

They are defined in *userapp.** properties. By default they do nothing and point to the localhost but you can change them accordingly if needed.

```yaml
userapp:
  # is executed when the /api/receipt/verify endpoint is called
  handle-receipt-update-url: <YOUR_APP_REST_API_HANDLER>

  # is executed when the /api/subscription/statusUpdateNotification endpoint is called
  handle-subscription-update-url: <YOUR_APP_REST_API_HANDLER>
```

### Security

Client is required to pass an API key defined in 
 
```yaml
yaakapp:
  api-key: <APP_SECRET_API_KEY>
```
 
in the *Authorization* HTTP header in order to call any endpoint.


### API Endpoints

#### Receipt

The following API endpoints accept HTTP POST method and require *Content-Type: application/json* [request body](https://developer.apple.com/documentation/appstorereceipts/requestbody)

* http://localhost:8080/api/receipt

Returns HTTP 200 with the detailed receipt body or HTTP 500 with error details in case of any errors

* http://localhost:8080/api/receipt/verify 

Simply returns HTTP code without response body. HTTP 200 if the receipt is valid or HTTP 500 in case of any errors.


Example request body:

```json
{
"receipt-data" : "MIImrwYJKoZIhvcNAQcCoIImoDCCJpwCAQExCzAJBgUrDgMCGgUAMIIWUAYJKoZIhvc...",
"password" : "ae3f0f3ece964b20x3fgt5",
"exclude-old-transactions" : "false"
}
```

Example HTTP 200 response body for /api/receipt endpoint:

```json
{
    "status": 0,
    "environment": "Sandbox",
    "receipt": {
        "receipt_type": "ProductionSandbox",
        "adam_id": 0,
        "app_item_id": 0,
        "bundle_id": "com.your.BundleId",
        "application_version": "201906111417",
        "download_id": 0,
        "version_external_identifier": 0,
        "receipt_creation_date": "2019-06-11 13:50:04 Etc/GMT",
        "receipt_creation_date_ms": "1560261004000",
        "receipt_creation_date_pst": "2019-06-11 06:50:04 America/Los_Angeles",
        "request_date": "2019-06-12 12:19:21 Etc/GMT",
        "request_date_ms": "1560341961112",
        "request_date_pst": "2019-06-12 05:19:21 America/Los_Angeles",
        "original_purchase_date": "2013-08-01 07:00:00 Etc/GMT",
        "original_purchase_date_ms": "1375340400000",
        "original_purchase_date_pst": "2013-08-01 00:00:00 America/Los_Angeles",
        "original_application_version": "1.0",
        "in_app": []
    },
    "latest_receipt": "...",
    "latest_receipt_info": [
        {
            "quantity": "1",
            "product_id": "com.your.ProductId",
            "transaction_id": "1000000536008625",
            "original_transaction_id": "1000000515833291",
            "purchase_date": "2019-06-11 14:10:37 Etc/GMT",
            "purchase_date_ms": "1560262237000",
            "purchase_date_pst": "2019-06-11 07:10:37 America/Los_Angeles",
            "original_purchase_date": "2019-04-03 10:37:27 Etc/GMT",
            "original_purchase_date_ms": "1554287847000",
            "original_purchase_date_pst": "2019-04-03 03:37:27 America/Los_Angeles",
            "expires_date": "2019-06-11 14:15:37 Etc/GMT",
            "expires_date_ms": "1560262537000",
            "expires_date_pst": "2019-06-11 07:15:37 America/Los_Angeles",
            "web_order_line_item_id": "1000000044901571",
            "is_trial_period": "false",
            "is_in_intro_offer_period": "false"
        }
    ],
    "pending_renewal_info": [
        {
            "expiration_intent": "1",
            "auto_renew_product_id": "com.your.ProductId",
            "original_transaction_id": "1000000515833291",
            "is_in_billing_retry_period": "0",
            "product_id": "com.your.ProductId",
            "auto_renew_status": "0"
        }
    ],
    "status_info": {
        "code": 0,
        "description": "The receipt is valid"
    },
    "is-retryable": false
}
```

Example HTTP 500 response body for /api/receipt endpoint:

```json
{
  "status":21010,
  "environment":"sandbox",
  "status_info":
    {
      "code":21010,
      "description":"Internal data access error. Try again later"
    },
  "is-retryable":true
}
```

#### Subscription

The following API endpoint accept HTTP POST method and returns HTTP 200. 
You should set it up as URL for subscription status updates notifications from the App Store.

* http://localhost:8080/api/subscription/statusUpdateNotification

### Documentation

* https://developer.apple.com/documentation/appstorereceipts/verifyreceipt

* https://developer.apple.com/documentation/storekit/in-app_purchase/enabling_status_update_notifications

### Issues and contribution

Bug reports and pull requests are welcome on GitHub at https://github.com/dietmap/yaak

### License

YAAK is Open Source software released under [MIT license](./LICENSE.txt).