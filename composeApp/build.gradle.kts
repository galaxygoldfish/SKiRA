import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

group = "com.skira.app"
version = "1.0.0"

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
                implementation("com.formdev:flatlaf:3.2.5")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
                implementation("org.apache.pdfbox:pdfbox:2.0.29")
                implementation("com.squareup.okhttp3:okhttp:4.10.0")
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
        mainClass = "com.skira.app.MainKt"
        nativeDistributions {
            javaHome = "C:/Program Files/Eclipse Adoptium/jdk-21.0.5.11-hotspot"
            targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Dmg)
            packageName = "SKiRA"
            packageVersion = project.version.toString()

           windows {
               menu = true
               menuGroup = "Generate Executable Apps"
               shortcut = true
               iconFile.set(file("src/jvmMain/composeResources/drawable/skira_outer_icon.ico"))
           }

            includeAllModules = true

            buildTypes.release {
                proguard {
                    isEnabled.set(false)
                }
            }
        }
    }
}
