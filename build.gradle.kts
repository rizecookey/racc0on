plugins {
    id("java")
    application
}

group = "net.rizecookey.racc0on"
version = "1.0-SNAPSHOT"

application {
    mainModule = "net.rizecookey.racc0on"
    mainClass = "net.rizecookey.racc0on.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jspecify:jspecify:1.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(24)
}

tasks.test {
    useJUnitPlatform()
}