import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

group = property("GROUP").toString()
version = providers.gradleProperty("VERSION_NAME").get()

android {
    namespace = "io.github.krishnapensalwar.devkit"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
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
    }

    /*
     * Required for Kotlin 1.9.x when the Compose compiler
     * plugin is not applied.
     */
    if (!pluginManager.hasPlugin("org.jetbrains.kotlin.plugin.compose")) {
        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.14"
        }
    }

    /*
     * Publish the Android RELEASE variant.
     *
     * This automatically provides:
     * - release AAR
     * - sources JAR
     */
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}

dependencies {

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    ksp(libs.androidx.room.compiler)

    // Compose
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.compose.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)

    // Network
    api(libs.okhttp)

    implementation(libs.coil.compose)

    api(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.serialization)

    // Tests
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}