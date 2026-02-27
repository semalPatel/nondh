plugins {
    kotlin("multiplatform") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("org.jetbrains.compose") version "1.9.2"
    id("com.android.kotlin.multiplatform.library") version "9.0.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
    id("app.cash.sqldelight") version "2.2.1"
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "nondh.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
    }
}

sqldelight {
    databases {
        create("NondhDatabase") {
            packageName.set("nondh.shared.db")
            sourceFolders.set(listOf("sqldelight"))
        }
    }
}
