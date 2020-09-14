import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.2.4.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	kotlin("jvm") version "1.3.61"
	kotlin("plugin.spring") version "1.3.61"
	id("com.google.cloud.tools.jib") version "1.8.0"
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
	implementation("org.springframework.retry:spring-retry:1.2.4.RELEASE")
	implementation("net.logstash.logback:logstash-logback-encoder:6.2")

	implementation("org.projectreactor:reactor-spring:1.0.1.RELEASE")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.google.apis:google-api-services-androidpublisher:v3-rev130-1.25.0")
	implementation("io.github.microutils:kotlin-logging:1.7.8")

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
		ports = listOf("8080", "8778", "9090")
	}
}