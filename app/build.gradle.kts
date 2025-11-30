import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
    kotlin("kapt") // Enable KAPT for Glide annotation processing
}

// Load API keys from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

// Get API keys with fallback to empty string (will cause compile error if missing)
val googlePlacesApiKey: String = localProperties.getProperty("GOOGLE_PLACES_API_KEY") ?: ""
val supabaseUrl: String = localProperties.getProperty("SUPABASE_URL") ?: ""
val supabaseAnonKey: String = localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""

android {
    namespace = "ca.gbc.comp3074.uiprototype"
    compileSdk = 36

    defaultConfig {
        applicationId = "ca.gbc.comp3074.uiprototype"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Performance optimizations for high refresh rate
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        
        // Inject API keys into BuildConfig
        buildConfigField("String", "GOOGLE_PLACES_API_KEY", "\"$googlePlacesApiKey\"")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        
        // Inject Google Maps API key into AndroidManifest
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = googlePlacesApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            
            // Performance optimizations for release builds
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
        debug {
            // For development performance testing
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            applicationIdSuffix = ".debug"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    // Enable view binding for better performance
    buildFeatures {
        viewBinding = true
        buildConfig = true  // Enable BuildConfig generation for API keys
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.4")
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1") // Use kapt for Room
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    
    // Maps & Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-places:17.1.0")
    
    // Google Places API & HTTP Client
    implementation("com.google.android.libraries.places:places:3.5.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Additional performance optimizations
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    
    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
    // Supabase Dependencies
    implementation(platform("io.github.jan-tennert.supabase:bom:2.6.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    
    // Ktor client for Supabase
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-utils:2.3.12")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // Image Loading - Glide with annotation processor for custom config
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0") // Use kapt for Kotlin projects
}