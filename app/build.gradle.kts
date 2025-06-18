plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.greenbuyapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.greenbuyapp"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



    // Support libraries
    implementation("androidx.browser:browser:1.5.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.5.5")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.paging:paging-runtime-ktx:2.1.2")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.work:work-runtime-ktx:2.9.1")


    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-service:2.5.1")

// Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")


// Billing
    implementation("com.android.billingclient:billing-ktx:6.0.1")

// Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

// OkHttp
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.10")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.10")

// Okio
    implementation("com.squareup.okio:okio:3.2.0")

// Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

// Moshi
    implementation("com.squareup.moshi:moshi:1.15.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

// Glide
    implementation("com.github.bumptech.glide:glide:4.13.2")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    kapt("com.github.bumptech.glide:compiler:4.13.2")

// Koin
    implementation("io.insert-koin:koin-android:3.3.3")
    implementation("io.insert-koin:koin-androidx-workmanager:3.3.3")

// Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics-ktx:21.2.0")
    implementation("com.google.firebase:firebase-crashlytics:18.3.5")
    implementation("com.google.firebase:firebase-inappmessaging-ktx:20.1.3") {
        exclude(group = "io.grpc", module = "grpc-okhttp")
    }
    implementation("io.grpc:grpc-okhttp:1.52.1")
    implementation("com.google.firebase:firebase-firestore-ktx:24.4.4")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

// View binding
    implementation("com.github.kirich1409:viewbindingpropertydelegate:1.5.6")

// Lottie
    implementation("com.airbnb.android:lottie:5.2.0")

// PhotoView
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

// Pager indicator
    implementation("com.github.intik:overflow-pager-indicator:3.1.0")

// PrettyTime
    implementation("org.ocpsoft.prettytime:prettytime:5.0.4.Final")

// Open source notices
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")

// Muzei
    implementation("com.google.android.apps.muzei:muzei-api:3.4.1")

// LeakCanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.9.1")

    //sdp
    implementation ("com.intuit.sdp:sdp-android:1.1.1")

    //searchbar
    implementation ("io.github.lapism:search:2.0.1")

    //bottom navigation
    implementation ("com.tbuonomo:morph-bottom-navigation:1.0.0")
}
