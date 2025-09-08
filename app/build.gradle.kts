
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.kodomo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kodomo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }





    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

}

dependencies {
    // Core Android components
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.localbroadcastmanager)

    // Location Services
    implementation(libs.play.services.location)

    // Networking & Parsing
    implementation(libs.jsoup)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.converter.scalars)

    // Document file support for storage operations
    implementation(libs.androidx.documentfile)

    // Material Design
    implementation(libs.androidx.material3.android)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Data Binding
    implementation(libs.androidx.databinding.adapters)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Firebase/Credentials/Google Auth (from 'new')
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Google Play Services Auth
    implementation(libs.play.services.auth)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // implementation(libs.firebase.ui.auth) // Uncomment if needed
}