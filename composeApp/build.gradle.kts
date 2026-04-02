import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import java.io.File

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

group = "com.skira.app"
version = findProperty("appVersion") as String? ?: "1.0.0"

// ── AppVersion code-gen ───────────────────────────────────────────────────────
// Writes utilities/AppVersion.kt into the build directory so the version string
// is always in sync with gradle.properties without any manual edits to source.
val generateAppVersionFile by tasks.registering {
    val appVersion = project.version.toString()
    val outputDir  = layout.buildDirectory.dir("generated/appVersion/jvmMain/kotlin/com/skira/app/utilities")
    inputs.property("appVersion", appVersion)
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("AppVersion.kt").writeText(
            """
            |package com.skira.app.utilities
            |
            |/** Auto-generated — do not edit. Bump [appVersion] in gradle.properties instead. */
            |object AppVersion {
            |    const val CURRENT = "$appVersion"
            |}
            """.trimMargin()
        )
    }
}
// ─────────────────────────────────────────────────────────────────────────────

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "21"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.animation)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.compose.shimmer)
                implementation(libs.kotlinx.serialization.json)
                implementation("com.formdev:flatlaf:3.7.1")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmMain by getting {
            // Include the auto-generated AppVersion.kt
            kotlin.srcDir(generateAppVersionFile.map { it.outputs.files.singleFile.parentFile.parentFile.parentFile })
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.foundation)
                implementation(libs.kotlinx.coroutinesSwing)
                implementation("org.apache.pdfbox:pdfbox:2.0.29")
                implementation("com.squareup.okhttp3:okhttp:4.10.0")
                implementation("org.apache.xmlgraphics:batik-transcoder:1.17")
            }
        }
    }
}

compose {
    // Keep compiler aligned with Compose runtime
    kotlinCompilerPlugin.set("1.9.2")
}

compose.desktop {
    application {
        val requestedJavaHome = providers.gradleProperty("skira.javaHome").orNull
            ?: System.getenv("JAVA_HOME")
        val fallbackMacJbrSdk = "/Library/Java/JavaVirtualMachines/jbrsdk-21.0.10-osx-aarch64-b1163.110/Contents/Home"
        val resolvedJavaHome = listOfNotNull(requestedJavaHome, fallbackMacJbrSdk)
            .firstOrNull { File(it).isDirectory }
        if (resolvedJavaHome != null) {
            javaHome = resolvedJavaHome
        }

        mainClass = "com.skira.app.MainKt"
        jvmArgs += listOf("--enable-native-access=ALL-UNNAMED")
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Dmg)
            packageName = "SKiRA"
            packageVersion = project.version.toString()

            windows {
                menu = true
                menuGroup = "Generate Executable Apps"
                shortcut = true
                iconFile.set(file("src/jvmMain/composeResources/drawable/skira_outer_icon.ico"))
            }

            macOS {
                iconFile.set(file("src/jvmMain/composeResources/drawable/skira_icon_mac.icns"))
                dockName = "SKiRA"
            }

            includeAllModules = false

            buildTypes.release {
                proguard {
                    isEnabled.set(false)
                }
            }
        }

        tasks.withType<JavaExec>().configureEach {
            if (resolvedJavaHome != null) {
                executable = File(resolvedJavaHome, "bin/java").absolutePath
            }
            jvmArgs("--enable-native-access=ALL-UNNAMED")
        }

        tasks.withType<Test>().configureEach {
            jvmArgs("--enable-native-access=ALL-UNNAMED")
        }
    }
}
