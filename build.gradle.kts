import java.io.ByteArrayOutputStream
import java.net.URI

plugins {
    id("org.openstreetmap.josm") version "0.8.2"
    kotlin("jvm") version "1.9.24"
    jacoco
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.compileJava {
    options.release = 17
}

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/gabortim/josm-libphonenumber")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: providers.gradleProperty("GITHUB_ACTOR").get()
            password = System.getenv("GH_PACKAGE_REPO_TOKEN") ?: providers.gradleProperty("GH_PACKAGE_REPO_TOKEN").get()
        }
    }
}

sourceSets {
    main {
        i18n.po.setSrcDirs(setOf("src/main/resources/data/i18n"))
    }
}

version = "1.2.0"
val versionFile = "version.txt"

josm {
    pluginName = "phonenumber"
    josmCompileVersion = "19207"
    manifest {
        author = "gaben"
        description = "Gives the validator ability to verify and auto-fix incorrect phone numbers"
        pluginDependencies.add("libphonenumber")
        minJosmVersion = "18475" // due to PatternUtils
        minJavaVersion = 17
        canLoadAtRuntime = true
        mainClass = "com.github.gabortim.phonenumber.PhoneNumberPlugin"
        iconPath = "images/icon.svg"
        website = URI("https://github.com/gabortim/josm-phonenumber").toURL()
    }
}

/**
 * Returns git revision number
 * @return returns git revision number
 */
fun getGitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

dependencies {
    packIntoJar(kotlin("stdlib"))

    implementation("org.openstreetmap.josm.plugins:libphonenumber:8.+") { isChanging = true }

    // https://mvnrepository.com/artifact/org.wiremock/wiremock
    testImplementation("org.wiremock:wiremock:3.5.4")
    testImplementation(kotlin("reflect"))

    val junit = "5.11.2"
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junit}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junit}")
    testImplementation("org.openstreetmap.josm:josm-unittest:SNAPSHOT") { isChanging = true }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.200".toBigDecimal()
            }
        }
    }
    dependsOn(tasks.jacocoTestReport)
}

tasks.register("storeVersion") {
    // doLast is needed for the cleanup task
    doLast {
        File(versionFile).writeText("$version")
    }
}

tasks.jar {
    from("images/**")
    from("README.md")
    from("LICENSE")

    dependsOn("storeVersion")
}

tasks.clean {
    delete(versionFile)
}