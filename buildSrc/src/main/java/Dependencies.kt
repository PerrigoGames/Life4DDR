object Versions {
    const val min_sdk = 21
    const val target_sdk = 30
    const val compile_sdk = 30

    const val kotlin = "1.5.20"
    const val cocoapodsext = "0.12"
    const val espresso = "3.1.0"
    const val googleServices = "4.3.5"
    const val sqlDelight = "1.5.1"
    const val kermit = "0.1.8"
    const val ktor = "1.5.2"
    const val stately = "1.1.4"
    const val ktlint_gradle_plugin = "9.4.1"
    const val lottie = "3.3.0"
    const val eventbus = "3.1.1"
    const val dexter = "6.2.2"
    const val glide = "4.9.0"
    const val robolectric = "4.5.1"
    const val firebase = "27.0.0"
    const val stetho = "1.6.0"
    const val multiplatformSettings = "0.7.4"
    const val coroutines = "1.3.3-native-mt"
    const val koin = "3.0.0-alpha-4"
    const val kotlinxDateTime = "0.1.1"

    object Android {
        const val build_tools_version = "30.0.3"
        const val gradle_plugin = "4.1.1"
    }

    object AndroidX {
        const val activity = "1.2.0-alpha05"
        const val appcompat = "1.2.0"
        const val constraintlayout = "2.0.4"
        const val core = "1.3.2"
        const val fragment = "1.3.0-alpha05"
        const val lifecycle = "2.2.0"
        const val material = "1.3.0"
        const val preference = "1.1.1"
        const val recyclerview = "1.2.0"
        const val swipeRefresh = "1.1.0"
        const val test = "1.3.0"
        const val test_ext = "1.1.2"
    }
}

object Deps {
    const val android_gradle_plugin = "com.android.tools.build:gradle:${Versions.Android.gradle_plugin}"
    const val google_services = "com.google.gms:google-services:${Versions.googleServices}"
    const val cocoapodsext = "co.touchlab:kotlinnativecocoapods:${Versions.cocoapodsext}"

    const val stetho = "com.facebook.stetho:stetho:${Versions.stetho}"
    const val kermit = "co.touchlab:kermit:${Versions.kermit}"

    const val kotlinxDateTime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinxDateTime}"
    const val lottie = "com.airbnb.android:lottie:${Versions.lottie}"
    const val eventbus = "org.greenrobot:eventbus:${Versions.eventbus}"
    const val dexter = "com.karumi:dexter:${Versions.dexter}"
    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glide_compiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    const val stately =  "co.touchlab:stately-common:${Versions.stately}"
    const val multiplatformSettings =  "com.russhwolf:multiplatform-settings:${Versions.multiplatformSettings}"
    const val multiplatformSettingsTest = "com.russhwolf:multiplatform-settings-test:${Versions.multiplatformSettings}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    const val koinCore = "org.koin:koin-core:${Versions.koin}"
    const val koinTest = "org.koin:koin-test:${Versions.koin}"

    object Firebase {
        const val bom = "com.google.firebase:firebase-bom:${Versions.firebase}"
        const val messaging = "com.google.firebase:firebase-messaging-ktx"
        const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
        const val analytics = "com.google.firebase:firebase-analytics-ktx"
    }

    object AndroidX {
        const val activity = "androidx.activity:activity-ktx:${Versions.AndroidX.activity}"
        const val appcompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appcompat}"
        const val core_ktx = "androidx.core:core-ktx:${Versions.AndroidX.core}"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.constraintlayout}"
        const val fragment = "androidx.fragment:fragment-ktx:${Versions.AndroidX.fragment}"
        const val fragment_testing = "androidx.fragment:fragment-testing:${Versions.AndroidX.fragment}"
        const val material = "com.google.android.material:material:${Versions.AndroidX.material}"
        const val preferences = "androidx.preference:preference:${Versions.AndroidX.preference}"
        const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.AndroidX.recyclerview}"
        const val swipeRefresh = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.AndroidX.swipeRefresh}"

        const val lifecycle_runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.AndroidX.lifecycle}"
        const val lifecycle_viewmodel = "androidx.lifecycle:lifecycle-viewmodel:${Versions.AndroidX.lifecycle}"
        const val lifecycle_viewmodel_extensions = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.lifecycle}"
        const val lifecycle_livedata = "androidx.lifecycle:lifecycle-livedata:${Versions.AndroidX.lifecycle}"
        const val lifecycle_extension = "androidx.lifecycle:lifecycle-extensions:${Versions.AndroidX.lifecycle}"
        const val koin_viewmodel = "org.koin:koin-androidx-viewmodel:${Versions.koin}"
    }

    object AndroidXTest {
        const val core =  "androidx.test:core:${Versions.AndroidX.test}"
        const val junit =  "androidx.test.ext:junit:${Versions.AndroidX.test_ext}"
        const val runner = "androidx.test:runner:${Versions.AndroidX.test}"
        const val rules = "androidx.test:rules:${Versions.AndroidX.test}"
        const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    }

    object KotlinTest {
        const val common =      "org.jetbrains.kotlin:kotlin-test-common:${Versions.kotlin}"
        const val annotations = "org.jetbrains.kotlin:kotlin-test-annotations-common:${Versions.kotlin}"
        const val jvm =         "org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}"
        const val junit =       "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
        const val reflect =     "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    }
    object Coroutines {
        const val common = "org.jetbrains.kotlinx:kotlinx-coroutines-core-common:${Versions.coroutines}"
        const val jdk = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
        const val native = "org.jetbrains.kotlinx:kotlinx-coroutines-core-native:${Versions.coroutines}"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    }
    object SqlDelight{
        const val gradle = "com.squareup.sqldelight:gradle-plugin:${Versions.sqlDelight}"
        const val runtime = "com.squareup.sqldelight:runtime:${Versions.sqlDelight}"
        const val coroutinesExtensions = "com.squareup.sqldelight:coroutines-extensions:${Versions.sqlDelight}"
        const val runtimeJdk = "com.squareup.sqldelight:runtime-jvm:${Versions.sqlDelight}"
        const val driverIos = "com.squareup.sqldelight:native-driver:${Versions.sqlDelight}"
        const val driverAndroid = "com.squareup.sqldelight:android-driver:${Versions.sqlDelight}"
    }
    object Ktor {
        const val commonCore = "io.ktor:ktor-client-core:${Versions.ktor}"
        const val commonJson = "io.ktor:ktor-client-json:${Versions.ktor}"
        const val commonLogging = "io.ktor:ktor-client-logging:${Versions.ktor}"
        const val androidCore = "io.ktor:ktor-client-okhttp:${Versions.ktor}"
        const val ios = "io.ktor:ktor-client-ios:${Versions.ktor}"
        const val commonSerialization ="io.ktor:ktor-client-serialization:${Versions.ktor}"
    }
}
