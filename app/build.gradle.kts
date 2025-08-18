plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.navigation.safe.args)
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
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    kapt("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.7.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(files("libs/palm-V1.0.1.aar"))

    implementation("androidx.cardview:cardview:1.0.0")
    implementation(files("libs/ysapi.jar"))
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // ✅ HTTP client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

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

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.biometric:biometric:1.1.0")
}
