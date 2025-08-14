plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.quickstorephilippinesandroidapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.quickstorelocker"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        ndkVersion = "25.1.8937393"

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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(files("libs/ysapi.jar"))
    implementation(files("libs/palm-V1.0.1.aar"))

    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.0")

    implementation("io.github.jan-tennert.supabase:storage-kt:2.0.0")
    // HTTP Client
    implementation("io.ktor:ktor-client-android:2.3.4")
    implementation("io.ktor:ktor-client-core:2.3.4")
// JSON Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ✅ Optional: Logging interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ✅ Coroutines support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // JSON (if using org.json)
    implementation("org.json:json:20240303")

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // ✅ Room Database (latest stable for Flow support)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // ✅ Room testing (version match)
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    // CameraX
    implementation("androidx.camera:camera-core:1.4.0")
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")
    implementation("com.google.guava:guava:31.1-android")
    // ML Kit Face Detection
    implementation("com.google.mlkit:face-detection:16.1.5")
    // For hashing
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.tensorflow:tensorflow-lite:2.13.0")


}
