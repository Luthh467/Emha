import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Mengambil timestamp dari root project jika ada, atau generate baru
val timestamp: String = (rootProject.extra["buildTimestamp"] as? String)
    ?: SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

android {
    namespace = "com.example.nutrimind"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nutrimind"
        minSdk = 26
        targetSdk = 35
        versionCode = (rootProject.extra["buildVersionCode"] as? Int) ?: SimpleDateFormat("yyMMddHH").format(Date()).toInt()
        versionName = "1.0.0-$timestamp"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            versionNameSuffix = "-debug"
        }
    }

    // Skrip otomatis untuk mengganti nama output file APK dengan menyertakan timestamp
    applicationVariants.all {
        val variantName = name
        outputs.all {
            val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output?.outputFileName = "NutriMind-${variantName}-${timestamp}.apk"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
