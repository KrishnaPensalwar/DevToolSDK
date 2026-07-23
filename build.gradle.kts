plugins {
    alias(devtoolLibs.plugins.android.library)
    alias(devtoolLibs.plugins.kotlin.android)
    alias(devtoolLibs.plugins.kotlin.ksp)
    alias(devtoolLibs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.devtool"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    kotlin {
        jvmToolchain(17)
    }
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(devtoolLibs.androidx.datastore.preferences)
    implementation(devtoolLibs.androidx.lifecycle.viewmodel.ktx)
    implementation(devtoolLibs.androidx.lifecycle.runtime.ktx)
    implementation(devtoolLibs.androidx.core.ktx)
    implementation(devtoolLibs.androidx.appcompat)
    implementation(devtoolLibs.material)
    
    // Room
    implementation(devtoolLibs.androidx.room.runtime)
    implementation(devtoolLibs.androidx.room.ktx)
    ksp(devtoolLibs.androidx.room.compiler)

    // Compose
    implementation(platform(devtoolLibs.androidx.compose.bom))
    implementation(devtoolLibs.androidx.compose.ui)
    implementation(devtoolLibs.androidx.compose.ui.graphics)
    implementation(devtoolLibs.androidx.compose.ui.tooling.preview)
    implementation(devtoolLibs.androidx.compose.material3)
    implementation(devtoolLibs.compose.icons.extended)
    implementation(devtoolLibs.androidx.activity.compose)
    implementation(devtoolLibs.androidx.lifecycle.runtime.ktx)
    debugImplementation(devtoolLibs.androidx.compose.ui.tooling)

    // OkHttp
    implementation(devtoolLibs.okhttp)

    // Coil for Image Loading
    implementation(devtoolLibs.coil.compose)

    testImplementation(devtoolLibs.junit)
    androidTestImplementation(devtoolLibs.androidx.espresso.core)
    androidTestImplementation(devtoolLibs.androidx.junit)

    implementation(devtoolLibs.ktor.client.android)
    implementation(devtoolLibs.ktor.client.content.negotiation)
    implementation(devtoolLibs.ktor.serialization.kotlinx.json)
    implementation(devtoolLibs.ktor.client.logging)
    implementation(devtoolLibs.ktor.client.serialization)
    implementation(devtoolLibs.ktor.client.core)

}
