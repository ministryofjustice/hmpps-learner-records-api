plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.1.2"
  kotlin("plugin.spring") version "2.0.21"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.1.0")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-jaxb:2.11.0")
  implementation("com.google.code.gson:gson:2.11.0")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("javax.xml.bind:jaxb-api:2.3.1")
  implementation("org.glassfish.jaxb:jaxb-runtime:2.3.5")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.1.0")
  testImplementation("org.wiremock:wiremock-standalone:3.9.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.24") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
  test {
    environment("PFX_FILE_PASSWORD", "TEST")
    environment("UK_PRN", "TEST")
    environment("ORG_PASSWORD", "TEST")
    environment("VENDOR_ID", "TEST")
  }
}
