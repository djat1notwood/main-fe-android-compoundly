import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.djatslabs.compoundly"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.djatslabs.compoundly"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val storePath = keystoreProperties.getProperty("storeFile")
            ?: System.getenv("COMPOUNDLY_KEYSTORE_PATH")
        if (!storePath.isNullOrBlank()) {
            val resolvedStoreFile = rootProject.file(storePath)
            if (resolvedStoreFile.exists()) {
                create("release") {
                    storeFile = resolvedStoreFile
                    storePassword = keystoreProperties.getProperty("storePassword")
                        ?: System.getenv("COMPOUNDLY_KEYSTORE_PASSWORD")
                    keyAlias = keystoreProperties.getProperty("keyAlias")
                        ?: System.getenv("COMPOUNDLY_KEY_ALIAS")
                    keyPassword = keystoreProperties.getProperty("keyPassword")
                        ?: System.getenv("COMPOUNDLY_KEY_PASSWORD")
                }
            }
        }
    }

    buildTypes {
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
