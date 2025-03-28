plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.versioning)
}

androidGitVersion {
    baseCode = 100000
    tagPattern = "^v[0-9]+\\.[0-9]+\\.[0-9]+$"
}

android {
    namespace = "dev.marcelsoftware.boosteroidplus"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.marcelsoftware.boosteroidplus"
        minSdk = 23
        targetSdk = 35
        defaultConfig {
            versionCode = androidGitVersion.code()
            versionName = androidGitVersion.name()
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
        allWarningsAsErrors = false
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    coreLibraryDesugaring(libs.desugaring)

    implementation(libs.toolbox.dynamicTheme)
    implementation(libs.toolbox.systemuicontroller)
    implementation(libs.toolbox.modalsheet)
    compileOnly(libs.xposed.legacy)
    implementation(libs.dexkit)
    implementation(libs.androidx.splashscreen)
    implementation(libs.composeIcons)

    ktlint(libs.ktlint.compose)
}

ktlint {
    android.set(true)
    outputColorName.set("RED")
}
