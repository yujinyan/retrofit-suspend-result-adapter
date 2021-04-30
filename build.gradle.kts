plugins {
  kotlin("jvm") version "1.5.0-RC"
}

group = "me.yujinyan"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  val retrofitVersion = "2.9.0"
  api("com.squareup.retrofit2:retrofit:$retrofitVersion")

  "4.4.3".also { version ->
    testImplementation("io.kotest:kotest-runner-junit5:$version")
    testImplementation("io.kotest:kotest-assertions-core:$version")
  }

  testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")
  "1.12.0".also { version ->
    testImplementation("com.squareup.moshi:moshi-kotlin:$version")
    testImplementation("com.squareup.moshi:moshi:$version")
  }
  testImplementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")

  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")

}


tasks.withType<Test> {
  useJUnitPlatform()
}

