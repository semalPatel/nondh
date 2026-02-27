plugins {
    id("com.android.application") version "9.0.1"
    id("org.jetbrains.compose") version "1.9.2"
}

android {
    namespace = "nondh.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "nondh.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
}
