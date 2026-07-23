plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ikan.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ikan.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
        val githubUrl = providers.gradleProperty("IKAN_GITHUB_URL").orElse("").get()
        val authorUrl = providers.gradleProperty("IKAN_AUTHOR_URL").orElse("").get()
        val updateJsonUrl = providers.gradleProperty("IKAN_UPDATE_JSON_URL")
            .orElse("https://raw.githubusercontent.com/daxiaamu/ikandroid/main/latest-release.json").get()
        buildConfigField("String", "GITHUB_URL", "\"$githubUrl\"")
        buildConfigField("String", "AUTHOR_URL", "\"$authorUrl\"")
        buildConfigField("String", "UPDATE_JSON_URL", "\"$updateJsonUrl\"")
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.datastore:datastore-preferences:1.2.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("org.jsoup:jsoup:1.22.1")
    implementation("io.coil-kt.coil3:coil-compose:3.4.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.4.0")

    implementation("androidx.media3:media3-exoplayer:1.10.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.10.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.10.1")
    implementation("androidx.media3:media3-ui:1.10.1")
    implementation("androidx.media3:media3-session:1.10.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20250517")
}
