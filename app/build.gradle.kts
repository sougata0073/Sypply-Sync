import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Kapt plugin
    id("kotlin-kapt")

    // Safe args
    id("androidx.navigation.safeargs.kotlin")

    // Google services plugin
    id("com.google.gms.google-services")

    // For parcelable
    id("kotlin-parcelize")
}

android {
    namespace = "com.sougata.supplysync"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sougata.supplysync"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables.useSupportLibrary = true

        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        buildConfigField("String", "SUPABASE_API_KEY", properties.getProperty("SUPABASE_API_KEY"))

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.annotation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Utility libraries
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    kapt(libs.androidx.lifecycle.compiler)

    // Navigation
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Coroutine
    implementation(libs.kotlinx.coroutines)

    // Firebase libraries
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.authentication)

    // UI libraries
    implementation(libs.mpandroidchart)
    implementation(libs.glide)
    implementation(libs.shimmer)
    implementation(libs.recyclerview.animators)

    // Animated bottom nav
    implementation(libs.smoothBottomBar)

    // IText PDF maker
    implementation(libs.iText)

    // Splash screen
    implementation(libs.androidx.core.splashscreen)

    // PDF viewer
    implementation(libs.android.pdf.viewer)
}