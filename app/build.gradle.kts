import com.android.build.api.dsl.ApplicationDefaultConfig
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.about.libraries)
}

val properties = Properties()

val keystorePropertiesFile = rootProject.file("key.properties")
if (keystorePropertiesFile.exists()) {
    properties.load(FileInputStream(keystorePropertiesFile))
}

val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(FileInputStream(localPropertiesFile))
}


android {
    namespace = "io.github.dracula101.jetscan"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.dracula101.jetscan"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ksp {
            // The location in which the generated Room Database Schemas will be stored in the repo.
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        buildConfigSecrets(this)
    }


    signingConfigs {
        create("release") {
            keyAlias = properties["keyAlias"].toString()
            keyPassword = properties["keyPassword"].toString()
            storePassword = properties["storePassword"].toString()
            storeFile = file(properties["storeFile"].toString())
        }
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_release"
            manifestPlaceholders["appRoundIcon"] = "@mipmap/ic_launcher_release_round"
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isShrinkResources = false
            isMinifyEnabled = false
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_debug"
            manifestPlaceholders["appRoundIcon"] = "@mipmap/ic_launcher_debug_round"
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
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
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerVersion.get()
    }
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {

    // ==================== SUB PROJECTS ====================
    implementation(projects.opencv)
    implementation(projects.pdf)

    // ==================== ANDROIDX ====================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.security.crypto.ktx)

    // ==================== COMPOSE ====================
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.compose.adaptive.android)

    // ==================== LIFECYCLE ====================
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.android.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.android.lifecycle.runtime.compose)
    implementation(libs.android.lifecycle.runtime.ktx)

    // ==================== MATERIAL  ====================
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ripple)
    implementation(libs.androidx.compose.icons.core)
    implementation(libs.androidx.compose.icons.extended)
    implementation(libs.androidx.ui.graphics)

    // ==================== BIOMETRICS ====================
    implementation(libs.androidx.biometrics)

    // ==================== COROUTINES ====================
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.play.services)

    // ==================== HILT ====================
    implementation(libs.dagger.hilt)
    implementation(libs.androidx.compose.adaptive.android)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.dagger.hilt.navigation.compose)

    // ==================== ROOM ====================
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // ==================== LIVE DATA ====================
    implementation(libs.androidx.liveData)

    // ==================== SHARED PREFERENCES ====================
    implementation(libs.androidx.sharedPrefs)

    // ==================== LOTTIE ====================
    implementation(libs.lottie)

    // ==================== COIL ====================
    implementation(libs.coil)

    // ==================== FIREBASE ====================
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    // ==================== GOOGLE ====================
    implementation(libs.google.auth)
    implementation(libs.google.mlkit.barcode)
    implementation(libs.google.mlkit.common)

    // ==================== HTTP CLIENT ====================
    implementation(libs.square.okhttp)
    implementation(libs.square.okhttp.logging)
    implementation(platform(libs.square.retrofit.bom))
    implementation(libs.square.retrofit)
    implementation(libs.gson)
    implementation(libs.square.retrofit.gson)

    // ==================== LOGGER ====================
    implementation(libs.timber)
    implementation(libs.logger)

    // ==================== CAMERAX ====================
    implementation(libs.camerax)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.camerax.extensions)

    // ==================== PAGING ====================
    implementation(libs.paging)
    implementation(libs.paging.compose)

    // ==================== ACCOMPANIST ====================
    implementation(libs.accompanist)

    // ==================== BOUQUET ====================
    implementation(libs.bouquet)

    // ==================== ZOOMABLE ====================
    implementation(libs.zoomable)

    // ==================== REORDERING ====================
    implementation(libs.reordering.compose)

    // ==================== COMPRESSOR ====================
    implementation(libs.compressor)

    // ==================== ABOUT LIBRARIES ====================
    implementation(libs.about.libraries.core)
    implementation(libs.about.libraries.ui)

    // ==================== JETBRAINS ====================
    // implementation(libs.document.ai)

    // ==================== TESTING ====================
    testImplementation(libs.junit)
    implementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.junit)

}

fun buildConfigSecrets(config: ApplicationDefaultConfig) {
    config.buildConfigField("String", "GOOGLE_CLIENT_ID", properties["GOOGLE_CLIENT_ID"].toString())
    config.buildConfigField("String", "SERVICE_ACCOUNT_TYPE", properties["SERVICE_ACCOUNT_TYPE"].toString())
    config.buildConfigField("String", "SERVICE_ACCOUNT_PROJECT_ID", properties["SERVICE_ACCOUNT_PROJECT_ID"].toString())
    config.buildConfigField("String", "SERVICE_ACCOUNT_PRIVATE_KEY_ID_BASE64_ENCODED", properties["SERVICE_ACCOUNT_PRIVATE_KEY_BASE64_ENCODED"].toString())
    config.buildConfigField("String", "SERVICE_ACCOUNT_PRIVATE_KEY", properties["SERVICE_ACCOUNT_PRIVATE_KEY"].toString())
    config.buildConfigField("String", "SERVICE_ACCOUNT_CLIENT_EMAIL", properties["SERVICE_ACCOUNT_CLIENT_EMAIL"].toString())
    config.buildConfigField("String", "SERVICE_ACCOUNT_CLIENT_ID", properties["SERVICE_ACCOUNT_CLIENT_ID"].toString())
    config.buildConfigField("String", "SERVICE_ACCOUNT_AUTH_URI", properties["SERVICE_ACCOUNT_AUTH_URI"].toString())
    config.buildConfigField("String", "SERVICE_ACCOUNT_TOKEN_URI", properties["SERVICE_ACCOUNT_TOKEN_URI"].toString())
    config.buildConfigField("String", "SERVICE_ACCOUNT_CLIENT_X509_CERT_URL", properties["SERVICE_ACCOUNT_CLIENT_X509_CERT_URL"].toString())
    config.buildConfigField("String", "SERVICE_ACCOUNT_UNIVERSE_DOMAIN", properties["SERVICE_ACCOUNT_UNIVERSE_DOMAIN"].toString())
    config.buildConfigField("String","GCP_DOCUMENT_AI_BASE_URL",properties["GCP_DOCUMENT_AI_BASE_URL"].toString())
    config.buildConfigField("String","GCP_DOCUMENT_AI_ENDPOINT",properties["GCP_DOCUMENT_AI_ENDPOINT"].toString())
}
