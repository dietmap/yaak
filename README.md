YAAK - Yet Another AppStore Kit
======

[![Build Status](https://travis-ci.com/dietmap/yaak.svg?branch=master)](https://travis-ci.com/dietmap/yaak)

#### YAAK is a simple server that by its API makes In-App Purchase receipt and Auto-Renewable subscription validation easy

You should always validate receipts on the server, in [Apple's words](https://developer.apple.com/library/ios/releasenotes/General/ValidateAppStoreReceipt/Chapters/ValidateRemotely.html#//apple_ref/doc/uid/TP40010573-CH104-SW1):
> Use a trusted server to communicate with the App Store. Using your own server lets you design your app to recognize and trust only your server, and lets you ensure that your server connects with the App Store server. It is not possible to build a trusted connection between a user’s device and the App Store directly because you don’t control either end of that connection.

### Running YAAK locally

YAAK is a [Spring Boot](https://github.com/spring-projects/spring-boot) application written in [Kotlin](https://github.com/kotlin) and built using [Gradle](https://github.com/gradle). 
You can build a jar file and then run on your local machine as follows:

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

Once you run it you can access YAAK here: http://localhost:8888/


### Configuration
There are two configuration profiles: *sandbox* and *production*. 

By default YAAK runs on *sandbox* profile therefor if you want to changed it you should set it as follows:

```bash
export SPRING_PROFILES_ACTIVE=production
```

and then run

```bash
$ ./gradlew bootRun
```


### Endpoints
The following endpoints are available:

* http://localhost:8888/api/receipt - returns the detailed receipt response 

* http://localhost:8888/api/receipt/verify - simply returns HTTP 200 if the receipt is valid and HTTP 4xx in other cases

Both of them accept HTTP POST method and require the following JSON payload:

```json
{
"receipt-data" : "<base64>",
"password" : "password",
"exclude-old-transactions" : "true/false"
}
```

### Documentation

### Stay in Touch

### License
YAAK is Open Source software released under [MIT license](./LICENSE.txt).