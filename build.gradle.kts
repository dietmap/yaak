import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.1.5.RELEASE"
	id("io.spring.dependency-management") version "1.0.7.RELEASE"
	kotlin("jvm") version "1.3.31"
	kotlin("plugin.spring") version "1.3.31"
	id("com.google.cloud.tools.jib") version "1.6.1"
}

group = "com.dietmap"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.retry:spring-retry:1.2.4.RELEASE")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

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
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	failFast = true
}

jib {
	to {
		image = "dietmap/yaak"
		auth {
			username = System.getenv("DOCKERHUB_USERNAME")
			password = System.getenv("DOCKERHUB_PASSWORD")
		}
	}
	container {
		labels = mapOf(
				"maintainer" to "Krzysztof Koziol"
		)
		jvmFlags = listOf("-Xms512m", "-Djava.awt.headless=true")
		mainClass = "com.dietmap.yaak.YaakApplication"
		ports = listOf("8080")
	}
}