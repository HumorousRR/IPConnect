import org.jetbrains.kotlin.types.expressions.GenericArrayClassLiteralSupport.Disabled.isEnabled

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.dss.ipcontrol"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dss.ipcontrol"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("haixin") {
            val path = properties.getValue("KEY_STORE_FILE") as String
            storeFile = file(path)
            storePassword = properties.getValue("KEY_PASSWORD_OS") as String
            keyAlias = properties.getValue("KEY_ALIAS_OS") as String
            keyPassword = properties.getValue("KEY_PASSWORD_OS") as String
            enableV3Signing = true
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("haixin")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("haixin")
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
        aidl = true
    }
    viewBinding {
        enable = true
    }

    flavorDimensions += "customer"
    productFlavors {
        create("general") {
            dimension = "customer"
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.ui.test.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(libs.androidx.startup.runtime)
    implementation(libs.gson)
    implementation(libs.okhttp)
}