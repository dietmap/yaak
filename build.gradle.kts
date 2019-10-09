import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.1.9.RELEASE"
	id("io.spring.dependency-management") version "1.0.8.RELEASE"
	kotlin("jvm") version "1.3.31"
	kotlin("plugin.spring") version "1.3.31"
	id("com.google.cloud.tools.jib") version "1.6.1"
	id("pl.allegro.tech.build.axion-release") version "1.10.2"
	id("com.gorylenko.gradle-git-properties") version "1.4.21"
}

apply(plugin = "com.gorylenko.gradle-git-properties")
apply(plugin = "pl.allegro.tech.build.axion-release")

scmVersion {
	versionIncrementer ("incrementPatch")
	versionCreator ("versionWithBranch")

}

group = "com.dietmap"
java.sourceCompatibility = JavaVersion.VERSION_1_8
version = scmVersion.version

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
	from {
		image = "gcr.io/distroless/java:11"
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
		jvmFlags = listOf("-Xms512m", "-Djava.awt.headless=true")
		mainClass = "com.dietmap.yaak.YaakApplicationKt"
		ports = listOf("8080")
	}
}