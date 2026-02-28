plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight")
}

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

kotlin {
    applyDefaultHierarchyTemplate()
    val iosX64 = iosX64()
    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64()
    val xcframework = XCFramework()

    listOf(iosX64, iosArm64, iosSimulatorArm64).forEach { target ->
        target.binaries.framework {
            baseName = "shared"
            isStatic = true
            xcframework.add(this)
        }
    }

    androidLibrary {
        namespace = "nondh.shared"
        compileSdk = 36
        minSdk = 26
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.compose.runtime:runtime:1.10.1")
                implementation("org.jetbrains.compose.foundation:foundation:1.10.1")
                implementation("org.jetbrains.compose.material3:material3:1.10.0-alpha05")
                implementation(compose.materialIconsExtended)
                implementation("io.ktor:ktor-client-core:3.1.0")
                implementation("io.ktor:ktor-client-content-negotiation:3.1.0")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:3.1.0")
                implementation("app.cash.sqldelight:android-driver:2.2.1")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.1.0")
                implementation("app.cash.sqldelight:native-driver:2.2.1")
            }
        }
    }
}

sqldelight {
    databases {
        create("NondhDatabase") {
            packageName.set("nondh.shared.db")
            srcDirs.from("src/commonMain/sqldelight")
        }
    }
    linkSqlite.set(true)
}
