YAAK - Yet Another AppStore Kit
======

[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/dietmap/yaak/build)](https://github.com/dietmap/yaak/actions)
[![Docker Image Version (latest semver)](https://img.shields.io/docker/v/dietmap/yaak)](https://hub.docker.com/r/dietmap/yaak/tags)

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


### Security

By default YAAK runs with this [*settings*](./src/main/resources/application.yml). 
You should generate your own **secure** API key or choose and configure other available security option: API_KEY, OAUTH, NONE

### Health check

Once the YAAK is started you can hit this endpoint in order to verify whether the service is up and running:
 
```bash
$ curl http://localhost:8080/actuator/health
```

### Adding custom callbacks

You can plug-in your custom [*webhook*](./src/main/resources/application.yml) for handling subscription/purchase specific logic.


```yaml
yaak.user-app.subscription-webhook-url
```

### Apple Store API Endpoints

#### Receipt

The following API endpoints accept HTTP POST method and require *Content-Type: application/json* [request body](https://developer.apple.com/documentation/appstorereceipts/requestbody)

* http://localhost:8080/api/appstore/receipts

Returns HTTP 200 with the detailed receipt body or HTTP 500 with error details in case of any errors

* http://localhost:8080/api/appstore/receipts/verify 

Simply returns HTTP code without response body. HTTP 200 if the receipt is valid or HTTP 500 in case of any errors.


#### Subscription

* http://localhost:8080/api/appstore/subscriptions/statusUpdateNotification

* http://localhost:8080/api/appstore/subscriptions/purchase

* http://localhost:8080/api/appstore/subscriptions/rewnew


### Google Play Store API Endpoints

#### Receipt

...

#### Subscription
...

### Docker

The docker image is stored in Docker Hub [dietmap/yaak](https://cloud.docker.com/repository/docker/dietmap/yaak) repo.

### Issues and contribution

Bug reports and pull requests are welcome on GitHub at https://github.com/dietmap/yaak

### License

YAAK is Open Source software released under [MIT license](./LICENSE.txt).