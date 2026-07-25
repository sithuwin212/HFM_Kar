plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.hfm.tv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hfm.tv"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = false
    }
}

dependencies {
    // Gson for JSON command parsing
    implementation("com.google.code.gson:gson:2.11.0")

    // Phone support (AppCompat)
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("androidx.fragment:fragment-ktx:1.8.3")

    // Leanback (Android TV UI)
    implementation("androidx.leanback:leanback:1.2.0")
    implementation("androidx.leanback:leanback-preference:1.2.0")
    implementation("androidx.tvprovider:tvprovider:1.1.0")

    // Media3 (ExoPlayer)
    implementation("androidx.media3:media3-exoplayer:1.5.0")
    implementation("androidx.media3:media3-ui:1.5.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // WebSocket Server
    implementation("org.java-websocket:Java-WebSocket:1.5.7")

    // SAF (Storage Access Framework)
    implementation("androidx.documentfile:documentfile:1.0.1")

    // Image Loading
    implementation("io.coil-kt:coil:2.7.0")

    // QR Code Generation
    implementation("com.google.zxing:core:3.5.3")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}

kapt {
    correctErrorTypes = true
}