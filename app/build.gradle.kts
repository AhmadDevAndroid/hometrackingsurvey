plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp.room)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.proto)
}

android {
    namespace = "com.app.householdtracing"
    compileSdk = 35
    flavorDimensions("environment")

    defaultConfig {
        applicationId = "com.app.householdtracing"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    productFlavors {
        create("stagging") {
            buildConfigField("String", "ENVIRONMENT", "\"stagging\"")
            applicationId = "com.app.householdtracing"
            buildConfigField("String", "BASE_URL", "\"https://pk-census-composer-new.surveyauto.com/api/v3/\"")
        }
        create("production") {
            buildConfigField("String", "ENVIRONMENT", "\"production\"")
            applicationId = "com.app.householdtracing"
            buildConfigField("String", "BASE_URL", "\"https://pk-census-composer-new.surveyauto.com/api/v3/\"")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
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

    implementation(libs.google.play.services)
    implementation(libs.work.manager)
    implementation(libs.okhttp.interceptor)
    implementation(libs.okhttp.main)
    implementation(libs.retrofit.scalars)
    implementation(libs.retrofit.main)
    implementation(libs.retrofit.convertor)
    implementation(libs.kotlin.coroutines)
    implementation (libs.kotlinx.coroutines.play.services)
    implementation(libs.runtime.permission)
    implementation(libs.timber)
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.preference.datastore)
    implementation(libs.androidx.datastore)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.ksp)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.proto.datastore)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.4"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins.create("java") {
                option("lite")
            }
        }
    }
}
