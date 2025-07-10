plugins {
    id("java")
    id("org.graalvm.buildtools.native") version "0.10.6"
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

    testImplementation("org.commonmark:commonmark:0.25.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(24)
}

tasks.test {
    useJUnitPlatform()
}

val createLauncherRun by tasks.register<CreateStartScripts>("createLauncherRun") {
    val startScripts = tasks.startScripts.get()
    applicationName = "racc0on-tester"
    mainModule = "net.rizecookey.racc0on"
    mainClass = "net.rizecookey.racc0on.utils.SimpleCompilerLauncher"
    classpath = startScripts.classpath
    outputDir = startScripts.outputDir
}

distributions {
    main {
        contents {
            from(createLauncherRun) {
                into("bin") {
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                }
            }
        }
    }
}