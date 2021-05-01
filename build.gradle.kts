import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.5.0-RC"
  `maven-publish`
}

group = "me.yujinyan"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

kotlin {
  explicitApi()
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
    }
  }
}


val retrofitVersion = "2.9.0"
val kotestVersion = "4.4.3"
val moshiVersion = "1.12.0"

dependencies {
  api("com.squareup.retrofit2:retrofit:$retrofitVersion")

  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")

  testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")
  testImplementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
  testImplementation("com.squareup.moshi:moshi:$moshiVersion")
  testImplementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")

  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}


tasks.withType<Test> {
  useJUnitPlatform()
}

