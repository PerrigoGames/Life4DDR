import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("io.fabric")
    id("com.google.gms.google-services")
    id("io.objectbox")
}

android {
    compileSdkVersion(Versions.compile_sdk)
    buildToolsVersion = Versions.buildToolsVersion
    defaultConfig {
        applicationId = "com.perrigogames.life4trials"
        minSdkVersion(Versions.min_sdk)
        targetSdkVersion(Versions.target_sdk)
        versionCode = 56
        versionName = "3.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        signingConfig = signingConfigs.findByName("upload")
    }
    packagingOptions {
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            manifestPlaceholders = mapOf(Pair("providerName", "com.perrigogames.fileprovider.debug"))
//            ext.enableCrashlytics = false
        }
        getByName("release")  {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            manifestPlaceholders = mapOf(Pair("providerName", "com.perrigogames.fileprovider"))
            signingConfig = signingConfigs.findByName("upload")
        }
    }
    signingConfigs {
        register("upload") {
            storeFile = file("../../keystores/upload-keystore.jks")
            keyPassword = "FlOnG9T#0sNe41bXjKtVJi75Y"
            storePassword = "FlOnG9T#0sNe41bXjKtVJi75Y"
            keyAlias = "upload"
        }
    }
    lintOptions {
        isAbortOnError = false
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    // Disable the google services plugin for debug
//    applicationVariants.firstOrNull { it.name.contains("debug") }.let { variant ->
//        project.tasks.getByName("process" + variant.name.capitalize() + "GoogleServices").enabled = false
//    }
}

dependencies {
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation(project(":shared"))
    implementation(Deps.recyclerview)
    implementation(Deps.core_ktx)
    implementation(Deps.preferences_x)
    implementation(Deps.app_compat_x)
    implementation(Deps.material_x)
    implementation(Deps.constraintlayout)
    implementation(Deps.lifecycle)
    implementation(Deps.viewmodel)
    implementation(Deps.firebase)
    implementation(Deps.firebase_messaging)
    implementation(Deps.firebase_crashlytics)

    implementation(Deps.koinCore)
    implementation(Deps.klock)
    implementation(Deps.lottie)
    implementation(Deps.eventbus)
    implementation(Deps.dexter)
    implementation(Deps.glide)
    implementation(Deps.gson)
//    debugImplementation(Deps.objectbox_debug)
//    releaseImplementation(Deps.objectbox_release)

    implementation(Deps.ktor.androidCore)
    implementation(Deps.SqlDelight.runtimeJdk)
    implementation(Deps.SqlDelight.driverAndroid)
    implementation(Deps.Coroutines.jdk)
    implementation(Deps.Coroutines.android)
    implementation(Deps.multiplatformSettings)
    implementation(Deps.koinCore)
    testImplementation(Deps.junit)

    kapt(Deps.glide_compiler)
}
