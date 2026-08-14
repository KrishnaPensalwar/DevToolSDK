plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    `maven-publish`
    signing
}

group = property("GROUP").toString()
version = providers.gradleProperty("VERSION_NAME").get()

android {
    namespace = "io.github.krishnapensalwar.devkit"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    // Only needed when the host uses Kotlin 1.9 (no compose compiler plugin).
    // Kotlin 2.0+ hosts should apply org.jetbrains.kotlin.plugin.compose instead.
    if (!pluginManager.hasPlugin("org.jetbrains.kotlin.plugin.compose")) {
        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.14"
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    jvmToolchain(17)
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

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.compose.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)

    api(libs.okhttp)
    implementation(libs.coil.compose)

    api(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.serialization)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                groupId = property("GROUP").toString()
                artifactId = property("POM_ARTIFACT_ID").toString()
                version = providers.gradleProperty("VERSION_NAME").get()

                pom {
                    name.set(property("POM_NAME").toString())
                    description.set(property("POM_DESCRIPTION").toString())
                    url.set(property("POM_URL").toString())
                    licenses {
                        license {
                            name.set(property("POM_LICENSE_NAME").toString())
                            url.set(property("POM_LICENSE_URL").toString())
                            distribution.set(property("POM_LICENSE_DIST").toString())
                        }
                    }
                    developers {
                        developer {
                            id.set(property("POM_DEVELOPER_ID").toString())
                            name.set(property("POM_DEVELOPER_NAME").toString())
                            email.set(property("POM_DEVELOPER_EMAIL").toString())
                        }
                    }
                    scm {
                        url.set(property("POM_SCM_URL").toString())
                        connection.set(property("POM_SCM_CONNECTION").toString())
                        developerConnection.set(property("POM_SCM_DEV_CONNECTION").toString())
                    }
                }
            }
        }

        repositories {
            maven {
                name = "MavenCentral"
                url = uri("https://central.sonatype.com/repository/maven-releases/")
                credentials {
                    username = providers.gradleProperty("mavenCentralUsername").orNull
                    password = providers.gradleProperty("mavenCentralPassword").orNull
                }
            }
        }
    }

    signing {
        val signingKey = providers.gradleProperty("signingInMemoryKey").orNull
        val signingPassword = providers.gradleProperty("signingInMemoryKeyPassword").orNull
        if (!signingKey.isNullOrEmpty() && !signingPassword.isNullOrEmpty()) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications)
        }
    }
}
