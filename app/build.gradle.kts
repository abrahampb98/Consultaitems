plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.example.Consultaitems"
    compileSdk = 34



    defaultConfig {
        applicationId = "com.example.Consultaitems"
        minSdk = 23
        targetSdk = 34
        versionCode = 1 ///cambiar aqui
        versionName = "1.0.5.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")

    // Lifecycle / Coroutines
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Android UI
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // CameraX
    implementation("androidx.camera:camera-core:1.2.3")
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    implementation("androidx.camera:camera-view:1.2.3")

    // ML Kit Barcode Scanner
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Network
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("org.json:json:20210307")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Ktor
    implementation("io.ktor:ktor-client-core:1.6.4")
    implementation("io.ktor:ktor-client-json-jvm:1.6.4")
    implementation("io.ktor:ktor-client-serialization-jvm:1.6.4")
    implementation("io.ktor:ktor-client-android:1.6.4")
    implementation("io.ktor:ktor-client-logging-jvm:1.6.4")

    // PDF
    implementation("com.itextpdf:itextg:5.5.10")
    implementation("com.itextpdf:itext7-core:7.1.15")

    // Images
    implementation("com.github.bumptech.glide:glide:4.13.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.13.2")
    implementation("androidx.exifinterface:exifinterface:1.3.3")

    // Maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:android-maps-utils:2.4.0")

    // Gráficos
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("com.google.android.flexbox:flexbox:3.0.0")

    implementation("com.github.chrisbanes:PhotoView:2.3.0")


}