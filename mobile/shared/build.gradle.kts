plugins {
    kotlin("multiplatform") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("org.jetbrains.compose") version "1.10.1"
    id("com.android.kotlin.multiplatform.library") version "9.0.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
    id("app.cash.sqldelight") version "2.2.1"
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
