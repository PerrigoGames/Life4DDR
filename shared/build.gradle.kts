import dev.icerock.gradle.MRVisibility

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqlDelight)
    id("dev.icerock.mobile.multiplatform-resources")
    id("dev.icerock.moko.kswift")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export("dev.icerock.moko:resources:0.23.0")
            export("dev.icerock.moko:graphics:0.9.0") // toUIColor here
            export("dev.icerock.moko:mvvm-core:0.16.1")
            export("dev.icerock.moko:mvvm-flow:0.16.1")
            export("dev.icerock.moko:mvvm-livedata:0.16.1")
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlin.time.ExperimentalTime")
            }
        }
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.coroutines.core)
            implementation(libs.sqlDelight.coroutinesExt)
            implementation(libs.bundles.ktor.common)
            implementation(libs.touchlab.stately)
            implementation(libs.multiplatformSettings.common)
            implementation(libs.multiplatformSettings.coroutines)
            implementation(libs.kotlinx.dateTime)
            implementation(libs.moko.mvvm.core)
            implementation(libs.moko.mvvm.flow)
            implementation(libs.ksoup.entities)
            api(libs.touchlab.kermit)
            api(libs.bundles.moko.mvvm.common)
            api(libs.bundles.moko.resources.common)
            api(libs.moko.kswift.runtime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.moko.resources.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.sqlDelight.android)
            implementation(libs.ktor.client.okHttp)
            implementation(libs.moko.mvvm.android.livedata.compose)
            implementation(libs.moko.mvvm.android.livedata.glide)
            implementation(libs.multiplatformSettings.datastore)
            implementation(libs.androidx.datastore)
            api(libs.moko.mvvm.core)
            api(libs.moko.mvvm.flow)
            api(libs.moko.mvvm.flow.compose)
        }
        iosMain.dependencies {
            implementation(libs.sqlDelight.native)
            implementation(libs.ktor.client.ios)
            api(libs.moko.mvvm.core)
            api(libs.moko.mvvm.flow)
        }
    }
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    namespace = "com.perrigogames.life4"

    sourceSets {
        getByName("main").java.srcDirs("build/generated/moko/androidMain/src")
    }
}

sqldelight {
    database("Life4Db") {
        packageName = "com.perrigogames.life4"
        dialect = "sqlite:3.24"
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "com.perrigogames.life4"
    multiplatformResourcesClassName = "MR"
    multiplatformResourcesVisibility = MRVisibility.Public
    iosBaseLocalizationRegion = "en"
    multiplatformResourcesSourceSet = "commonMain"
}

kswift {
    install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature)
    install(dev.icerock.moko.kswift.plugin.feature.PlatformExtensionFunctionsFeature)
    install(dev.icerock.moko.kswift.plugin.feature.DataClassCopyFeature)
}

tasks.matching { it.name == "generateMRcommonMain" }.configureEach {
    dependsOn(":copyDataFiles")
}