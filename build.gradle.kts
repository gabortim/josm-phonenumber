import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.net.URL

plugins {
    id("org.openstreetmap.josm") version "0.8.2"
    kotlin("jvm") version "1.9.10"
    jacoco
}

// Gradle toolchain does not allow compiling with JDK11 in Java 8 compatibility mode.
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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

version = "1.0.4"
val versionFile = "version.txt"

josm {
    pluginName = "phonenumber"
    josmCompileVersion = "18822"
    manifest {
        author = "gaben"
        description = "Gives the validator ability to verify and auto-fix incorrect phone numbers"
        pluginDependencies.add("libphonenumber")
        minJosmVersion = "17428"
        canLoadAtRuntime = true
        mainClass = "com.github.gabortim.phonenumber.PhoneNumberPlugin"
        iconPath = "images/icon.svg"
        website = URL("https://github.com/gabortim/josm-phonenumber")
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

    testImplementation("org.testng:testng") {
        version {
            strictly("7.5")
            because("TestNG 7.6 and up requires Java 11 or newer")
        }
    }
}

tasks.test {
    useTestNG()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
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