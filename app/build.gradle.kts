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

// app level properties
val keystorePropertiesFile = File(projectDir, "key.properties")
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
            signingConfig = signingConfigs.getByName("release")
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

    implementation(libs.about.libraries.core)
    implementation(libs.about.libraries.ui)
    implementation(libs.accompanist)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.biometrics)
    implementation(libs.androidx.compose.adaptive.android)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.icons.core)
    implementation(libs.androidx.compose.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ripple)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.security.crypto.ktx)
    implementation(libs.androidx.sharedPrefs)
    implementation(libs.androidx.splashscreen)
    implementation(libs.camerax)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.coil)
    implementation(libs.compressor)
    implementation(libs.dagger.hilt)
    implementation(libs.dagger.hilt.navigation.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.config)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.google.auth)
    implementation(libs.google.mlkit.barcode)
    implementation(libs.google.mlkit.common)
    implementation(libs.gson)
    implementation(libs.kotlin.coroutines)
    implementation(libs.lottie)
    implementation(platform(libs.square.retrofit.bom))
    implementation(libs.square.okhttp)
    implementation(libs.square.okhttp.logging)
    implementation(libs.square.retrofit)
    implementation(libs.square.retrofit.gson)
    implementation(libs.timber)
    implementation(libs.logger)
    implementation(libs.zoomable)
    implementation(libs.reordering.compose)
    ksp(libs.androidx.room.compiler)
    ksp(libs.dagger.hilt.compiler)

    testImplementation(libs.junit)
    implementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.junit)
}

fun buildConfigSecrets(config: ApplicationDefaultConfig) {
    config.buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${properties["GOOGLE_CLIENT_ID"]}\"")
    config.buildConfigField("String", "JETSCAN_BACKEND_URL", "\"${properties["JETSCAN_BACKEND_URL"]}\"")
}
