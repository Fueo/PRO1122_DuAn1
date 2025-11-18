plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.fa25_duan1"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.fa25_duan1"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL_ATHOME", "\"http://192.168.1.9:3002/\"")
            buildConfigField("String", "BASE_URL_ATSCHOOL", "\"http://172.16.120.66:3002/\"")
        }

        getByName("release") {
            isMinifyEnabled = true
            buildConfigField("String", "BASE_URL_ATHOME", "\"https://api.production.com/\"")
            buildConfigField("String", "BASE_URL_ATSCHOOL", "\"https://api.production.com/\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true  // BẬT tính năng BuildConfig
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.2.1")

    // Thư viện fetchAPI
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    // OkHttp logging
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
    // Thư viện quản lý dữ liệu
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    // Thư vin xử lý ảnh
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    // Thư viện UI
    implementation ("com.github.cachapa:ExpandableLayout:2.9.2")
    implementation ("com.github.arcadefire:nice-spinner:1.4.4")
}