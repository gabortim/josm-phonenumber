import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.josm)
    alias(libs.plugins.kotlin.jvm)
    jacoco
}

val pluginVersion: String by project
val josmCompileVersion: String by project
val minJosmVersion: String by project
val minJavaVersion: String by project
val author: String by project
val pluginDescription: String by project
val mainClass: String by project
val website: String by project

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = minJavaVersion
    }
}

tasks.compileJava {
    options.release = minJavaVersion.toInt()
}

tasks.compileTestJava {
    options.release = minJavaVersion.toInt()
}

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/gabortim/josm-libphonenumber")
        credentials {
            val localProperties = Properties().apply {
                val file = rootProject.file("local.properties")
                if (file.exists()) {
                    file.inputStream().use { inputStream ->
                        load(inputStream)
                    }
                }
            }

            username = System.getenv("GITHUB_ACTOR")
                ?: localProperties.getProperty("GITHUB_ACTOR")

            password = System.getenv("GITHUB_TOKEN")
                ?: localProperties.getProperty("GITHUB_PACKAGE_REPO_TOKEN")
        }
    }
}

sourceSets {
    main {
        i18n.po.setSrcDirs(setOf("src/main/resources/data/i18n"))
    }
}

version = pluginVersion

josm {
    pluginName = "phonenumber"
    this.josmCompileVersion = project.property("josmCompileVersion").toString()
    manifest {
        author = project.property("author").toString()
        description = project.property("pluginDescription").toString()
        pluginDependencies.add("libphonenumber")
        minJosmVersion = project.property("minJosmVersion").toString()
        minJavaVersion = project.property("minJavaVersion").toString().toInt()
        canLoadAtRuntime = true
        mainClass = project.property("mainClass").toString()
        iconPath = "images/icon.svg"
        website = URI(project.property("website").toString()).toURL()
    }
}

dependencies {
    packIntoJar(libs.kotlin.stdlib)

    implementation(libs.libphonenumber) { isChanging = true }

    testImplementation(libs.wiremock)
    testImplementation(libs.kotlin.reflect)

    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.josm.unittest) { isChanging = true }
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

tasks.jar {
    from("images/**")
    from("README.md")
    from("LICENSE")
}