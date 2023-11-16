plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-android")
    id ("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.sos"
    compileSdk = 34
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.sos"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions { exclude("META-INF/atomicfu.kotlin_module") }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.firebase:firebase-auth:22.2.0")
    val kotlin_version = "1.9.0"
    val activityVersion = "1.8.0"
    val appCompatVersion = "1.6.1"
    val constraintLayoutVersion = "2.1.4"
    val coreTestingVersion = "2.2.0"
    val coroutines = "1.7.1"
    val lifecycleVersion = "2.6.2"
    val materialVersion = "1.10.0"
    val roomVersion = "2.6.0"
    // testing
    val junitVersion = "4.13.2"
    val espressoVersion = "3.4.0"
    val androidxJunitVersion = "1.1.5"

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    implementation ("androidx.appcompat:appcompat:$appCompatVersion")
    implementation ("androidx.activity:activity-ktx:$activityVersion")

    // Dependencies for working with Architecture components
    // You'll probably have to update the version numbers in build.gradle (Project)

    // Room components
    implementation ("androidx.room:room-ktx:$roomVersion")
    ksp ("androidx.room:room-compiler:$roomVersion")
    androidTestImplementation ("androidx.room:room-testing:$roomVersion")

    // Lifecycle components
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation ("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")

    // Kotlin components
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines")

    // UI
    implementation ("androidx.constraintlayout:constraintlayout:$constraintLayoutVersion")
    implementation ("com.google.android.material:material:$materialVersion")

    // Testing
    testImplementation ("junit:junit:$junitVersion")
    androidTestImplementation ("androidx.arch.core:core-testing:$coreTestingVersion")
    androidTestImplementation ("androidx.test.espresso:espresso-core:$espressoVersion"
    ) {
        exclude(group = "com.android.support", module = "support-annotations")
    }
    androidTestImplementation ("androidx.test.ext:junit:$androidxJunitVersion")

    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
}