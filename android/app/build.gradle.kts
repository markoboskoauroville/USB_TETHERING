plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val appVersion = (project.findProperty("appVersion") as String? ?: "1").trim().toInt()

android {
    namespace = "community.auroville.usbtether"
    compileSdk = 35

    defaultConfig {
        applicationId = "community.auroville.usbtether"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersion
        versionName = appVersion.toString()
    }

    // The GitHub build signs with the saved key (secret ANDROID_DEBUG_KEYSTORE_B64 -> $DEBUG_KEYSTORE)
    System.getenv("DEBUG_KEYSTORE")?.let { ks ->
        signingConfigs.getByName("debug") {
            storeFile = file(ks); storePassword = "android"; keyAlias = "androiddebugkey"; keyPassword = "android"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    // Shizuku: lets the app run one shell command (svc usb setFunctions) without root.
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
}
