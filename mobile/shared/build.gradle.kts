plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight")
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

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
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

sqldelight {
    databases {
        create("NondhDatabase") {
            packageName.set("nondh.shared.db")
        }
    }
}
