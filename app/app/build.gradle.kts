import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

// Load secrets from local.properties
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.animesh.safeher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.animesh.safeher"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject secrets as BuildConfig fields
        buildConfigField("String", "MAPS_API_KEY",     "\"${localProps["MAPS_API_KEY"]}\"")
        buildConfigField("String", "FIREBASE_DB_URL",  "\"${localProps["FIREBASE_DB_URL"]}\"")
        buildConfigField("String", "FAST2SMS_API_KEY", "\"${localProps["FAST2SMS_API_KEY"]}\"")
        buildConfigField("String", "FCM_SERVER_KEY",   "\"${localProps["FCM_SERVER_KEY"]}\"")

        // Inject Maps key into Manifest for Maps SDK
        manifestPlaceholders["MAPS_API_KEY"] = localProps["MAPS_API_KEY"] ?: ""
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures {
        compose = true
        buildConfig = true  // needed for buildConfigField
    }
}

dependencies {
    implementation("androidx.compose.material3:material3")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.database)

    // Maps
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // Coroutines
    implementation(libs.coroutines.android)

    // Image loading
    implementation(libs.coil.compose)

    // Permissions
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.junit.ktx)

    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(kotlin("test"))
}