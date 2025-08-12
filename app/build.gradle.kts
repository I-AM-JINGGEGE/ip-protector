import java.io.FileInputStream
import java.util.Properties

// build properties
val buildProperties = Properties().apply {
    load(FileInputStream(rootProject.file("build.properties")))
}

// keystore properties
val keystoreProperties = Properties().apply {
    load(FileInputStream(rootProject.file("keystore.properties")))
}
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("android")

    id("com.google.android.gms.oss-licenses-plugin")
    id("kotlin-parcelize")
}

setupApp()

android {
    val applicationId: String by rootProject.extra
    val compileSdkVersion: Int by rootProject.extra
    val minSdkVersion: Int by rootProject.extra
    val targetSdkVersion: Int by rootProject.extra
    val javaVersion: JavaVersion by rootProject.extra
    val buildToolsVersion: String by rootProject.extra

    this.compileSdk = compileSdkVersion
    this.buildToolsVersion = buildToolsVersion

    signingConfigs {
        register("release") {
            storeFile = file(keystoreProperties["storeFile"]!!.toString())
            storePassword = keystoreProperties["storePassword"]!!.toString()
            keyAlias = keystoreProperties["keyAlias"]!!.toString()
            keyPassword = keystoreProperties["keyPassword"]!!.toString()
        }
    }

    lint {
        disable.add("InvalidPackage")
        checkReleaseBuilds = false
        abortOnError = false
    }

    namespace = "com.ironmeta.one"

    defaultConfig {
        this.applicationId = applicationId

        minSdk = minSdkVersion
        targetSdk = targetSdkVersion

        versionCode = buildProperties["versionCode"].toString().toInt()
        versionName = buildProperties["versionName"] as String

        buildConfigField("String", "PUBLISH_SENCONDS", "\"${System.currentTimeMillis() / 1000}\"")

        // for bundle
        ndk {
            abiFilters.clear()
            abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a"))
//            debugSymbolLevel = "FULL"
        }
        multiDexEnabled = true
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }

    packagingOptions {
        jniLibs.useLegacyPackaging = true
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )

            firebaseCrashlytics {
                mappingFileUploadEnabled = true
            }
        }

        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true

            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )

            firebaseCrashlytics {
                mappingFileUploadEnabled = false
            }
        }
    }

    flavorDimensions.add("channel")

    productFlavors {
        create("gp") {
            buildConfigField("String", "CNL", "\"gp\"")
            versionName = buildProperties["versionName"] as String
        }
        create("xiaomi") {
            buildConfigField("String", "CNL", "\"dtneaa1461b\"")
            versionName = "Xiaomi_" + buildProperties["versionName"]
        }
        create("transsion") {
            buildConfigField("String", "CNL", "\"dtn9a658c8e\"")
            versionName = "Transsion_" + buildProperties["versionName"]
        }
        create("samsung") {
            buildConfigField("String", "CNL", "\"dtn08376b2d\"")
            versionName = "Samsung_" + buildProperties["versionName"]
        }
    }
}

dependencies {
    val lifecycleVersion: String by rootProject.extra
    val firebaseBomVersion: String by rootProject.extra

    implementation("com.google.android.material:material:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")
    implementation("androidx.appcompat:appcompat:1.4.0")

    // androidx work manager
    implementation("androidx.work:work-runtime:2.7.1")

    // androidx lifecycle
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

    // utils
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.tencent:mmkv-static:1.2.14")

    implementation(platform("com.google.firebase:firebase-bom:$firebaseBomVersion"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ndk")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-config-ktx")

    // ironmeta tahiti sdk
//    implementation project(path: ':ironmeta')

    implementation("com.google.android.gms:play-services-ads-identifier:18.2.0")
    implementation("com.android.installreferrer:installreferrer:2.2")

    // ImageLoader
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // pag
    implementation("com.tencent.tav:libpag:4.0.5.82")

    //google admob
    implementation("com.google.android.gms:play-services-ads:22.0.0")

    // roiquery
    implementation("com.lovinjoy:datatowerai-core:3.0.1")

    implementation("com.appsflyer:af-android-sdk:6.15.2")

    implementation("com.github.JessYanCoding:AndroidAutoSize:v1.2.1")

    implementation("com.geyifeng.immersionbar:immersionbar:3.2.2")
    implementation("com.geyifeng.immersionbar:immersionbar-ktx:3.2.2")

    implementation("com.jaeger.statusbarutil:library:1.5.1")
    implementation("com.google.android.ump:user-messaging-platform:2.2.0")

//    //vpn sdk for google play bundle release
//    releaseImplementation('ironmeta:ironmeta-iphider-ss-rust-sdk-meta:nightly-20240711-1100-release') {
//        exclude group: "org.connectbot.jsocks", module: "jsocks"
//    }
//    //vpn sdk for debug
//    debugImplementation('ironmeta:ironmeta-iphider-ss-rust-sdk-meta:nightly-20240711-1030-debug') {
//        exclude group: "org.connectbot.jsocks", module: "jsocks"
//    }
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")
}
