plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.antoinehory.lejeudu5000"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.antoinehory.lejeudu5000"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.all {
            it.useJUnitPlatform() // Correct pour JUnit 5 dans les tests unitaires
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // BOM pour gérer les versions Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Dépendances de Test Unitaire
    testImplementation(libs.junit) // JUnit 4 (peut être nécessaire pour certains runners ou anciennes libs)
    testImplementation(libs.junit.jupiter.api) // JUnit 5 API (pour vos tests unitaires ViewModel)
    testRuntimeOnly(libs.junit.jupiter.engine) // JUnit 5 Engine (pour exécuter les tests JUnit 5)
    testImplementation(libs.junit.jupiter.params) // JUnit 5 Params (pour les tests paramétrés)
    testImplementation(libs.mockk.core) // MockK pour les tests unitaires
    testImplementation(libs.kotlinx.coroutines.test) // Pour tester les coroutines
    testImplementation(libs.turbine) // Pour tester les Flows

    // Dépendances de Test Instrumenté (AndroidTest)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // BOM pour les tests instrumentés Compose
    androidTestImplementation(libs.androidx.junit) // AndroidX Test JUnit runner et règles (basé sur JUnit 4)
    androidTestImplementation(libs.androidx.espresso.core) // Espresso (si vous l'utilisez, sinon optionnel)
    androidTestImplementation(libs.androidx.ui.test.junit4) // Compose UI Tests (essentiel)
    androidTestImplementation(libs.junit.jupiter.api) // Normalement non utilisé pour les tests @Composable qui tournent avec un runner JUnit 4
    androidTestImplementation(libs.mockk.android) // MockK pour les tests instrumentés

    // Dépendances de Debug
    debugImplementation(libs.androidx.ui.tooling) // Outils pour Compose (comme l'inspecteur de layout)
    debugImplementation(libs.androidx.ui.test.manifest) // Pour le manifest des tests UI Compose
}