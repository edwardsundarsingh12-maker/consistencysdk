plugins {
    alias(libs.plugins.android.library)      // ✅ Correct for SDK
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.kotlin.kapt")
}



android {
    namespace = "com.edapp.ed_sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Compose BOM (manages Compose versions)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation.core)
    implementation(libs.androidx.compose.material.core)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.activity.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelCompose)

    // Hilt
    implementation(libs.hilt.android.core)
    kapt(libs.hilt.compiler)

    // Hilt + Compose integration
    implementation(libs.androidx.hilt.navigation.compose)

    // Coil
    implementation(libs.coil.kt.compose)

    // Kolor
    implementation(libs.material.kolor)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.androidx.dataStore.preferences)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Logging
    implementation(libs.timber)

    // Build
    // SAtart Animation
    implementation("me.nikhilchaudhari:quarks:1.0.0-alpha02")
    implementation("nl.dionsegijn:konfetti-compose:2.0.2")
    implementation("nl.dionsegijn:konfetti-compose:2.0.5")
    // Testing
//    testImplementation(libs.junit4)
//    androidTestImplementation(libs.androidx.test.ext)
//    androidTestImplementation(libs.androidx.test.espresso.core)
//    androidTestImplementation(libs.androidx.compose.ui.test.junit)
//    debugImplementation(libs.androidx.compose.ui.tooling.core)
//    debugImplementation(libs.androidx.compose.ui.test.manifest)


    implementation(libs.composeIcons.cssGg)
    implementation(libs.composeIcons.weatherIcons)
    implementation(libs.composeIcons.evaIcons)
    implementation(libs.composeIcons.feather)
    implementation(libs.composeIcons.fontAwesome)
    implementation(libs.composeIcons.lineAwesome)
    implementation(libs.composeIcons.linea)
    implementation(libs.composeIcons.octicons)
    implementation(libs.composeIcons.simpleIcons)
    implementation(libs.composeIcons.tablerIcons)

    implementation("com.google.accompanist:accompanist-placeholder-material:0.36.0")
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("androidx.glance:glance-material3:1.1.0")

    // gson
    implementation(libs.gson)



    // Core Emoji support

}