import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.3.12.RELEASE"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	id("com.google.cloud.tools.jib") version "2.8.0"
	id("pl.allegro.tech.build.axion-release") version "1.11.0"
	id("com.gorylenko.gradle-git-properties") version "2.2.0"
}

apply(plugin = "com.gorylenko.gradle-git-properties")
apply(plugin = "pl.allegro.tech.build.axion-release")

scmVersion {
	versionIncrementer ("incrementPatch")
	versionCreator ("versionWithBranch")

}

group = "com.dietmap"
java.sourceCompatibility = JavaVersion.VERSION_11
version = scmVersion.version

repositories {
	mavenCentral()
}

dependencies {
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.security:spring-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework:spring-webflux")
	implementation("javax.validation:validation-api:2.0.1.Final")
	implementation("org.springframework.retry:spring-retry:1.3.3")
	implementation("net.logstash.logback:logstash-logback-encoder:7.2")

	implementation("org.projectreactor:reactor-spring:1.0.1.RELEASE")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.google.apis:google-api-services-androidpublisher:v3-rev142-1.25.0")
	implementation("io.github.microutils:kotlin-logging:3.0.0")
	implementation("io.springfox:springfox-boot-starter:3.0.0")
	implementation("org.zalando:logbook-spring-boot-starter:2.14.0")
	implementation("org.zalando:logbook-spring-boot-autoconfigure:2.14.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "junit")
	}
	testImplementation("org.junit.jupiter:junit-jupiter-api")
	testImplementation("org.junit.jupiter:junit-jupiter-params")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	failFast = true
}

jib {
	from {
		image = "gcr.io/distroless/java:11-debug"
	}
	to {
		image = "dietmap/yaak"
		tags = setOf(project.version.toString(), "latest")
		auth {
			username = System.getenv("DOCKERHUB_USERNAME")
			password = System.getenv("DOCKERHUB_PASSWORD")
		}
	}
	container {
		labels = mapOf(
				"maintainer" to "Krzysztof Koziol"
		)
		jvmFlags = listOf("-Djava.awt.headless=true")
		mainClass = "com.dietmap.yaak.YaakApplicationKt"
		ports = listOf("8080", "8081")
	}
}
