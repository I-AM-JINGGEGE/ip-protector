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
    val compileSdkVersion: Int by rootProject.extra
    val minSdkVersion: Int by rootProject.extra
    val targetSdkVersion: Int by rootProject.extra
    val javaVersion: JavaVersion by rootProject.extra
    val buildToolsVersion: String by rootProject.extra

    this.compileSdk = compileSdkVersion
    this.buildToolsVersion = buildToolsVersion
    ndkVersion = "27.2.12479018"

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

    namespace = "com.vpn.android"

    defaultConfig {
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

    flavorDimensions.add("develop_env")

    // 只保留 debugFlavorDebug 和 releaseFlavorRelease
    variantFilter {
        val names = flavors.map { it.name }
        if ((names.contains("debugFlavor") && buildType.name != "debug") ||
            (names.contains("releaseFlavor") && buildType.name != "release")) {
            ignore = true
        }
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
        // develop_env dimension flavors
        create("debugFlavor") {
            applicationId = "com.vpn.android.debug"
            dimension = "develop_env"
            manifestPlaceholders["ADMOB_APP_ID_VALUE"] = "ca-app-pub-3940256099942544~3347511713"
        }
        create("releaseFlavor") {
            applicationId = "com.free.ip.protector"
            dimension = "develop_env"
            manifestPlaceholders["ADMOB_APP_ID_VALUE"] = "ca-app-pub-3078276416195903~8454232095"
        }
        
        // channel dimension flavors
        create("gp") {
            dimension = "channel"
            buildConfigField("String", "CNL", "\"gp\"")
            versionName = buildProperties["versionName"] as String
        }
        create("xiaomi") {
            dimension = "channel"
            buildConfigField("String", "CNL", "\"dtneaa1461b\"")
            versionName = "Xiaomi_" + buildProperties["versionName"]
        }
        create("transsion") {
            dimension = "channel"
            buildConfigField("String", "CNL", "\"dtn9a658c8e\"")
            versionName = "Transsion_" + buildProperties["versionName"]
        }
        create("samsung") {
            dimension = "channel"
            buildConfigField("String", "CNL", "\"dtn08376b2d\"")
            versionName = "Samsung_" + buildProperties["versionName"]
        }
    }
}

dependencies {
    val lifecycleVersion: String by rootProject.extra
    val firebaseBomVersion: String by rootProject.extra

    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-ktx:1.10.1")

    // androidx work manager
    implementation("androidx.work:work-runtime:2.10.3")

    // androidx lifecycle
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // utils
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.tencent:mmkv-static:1.3.7") // do not modify mmkv version!
    implementation("com.getkeepsafe.relinker:relinker:1.4.5")

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
    implementation("com.github.bumptech.glide:glide:4.16.0")

    //google admob
    implementation("com.google.android.gms:play-services-ads:24.5.0")

    //roiquery
    implementation("ai.datatower:core:3.2.0")

    // https://mvnrepository.com/artifact/com.appsflyer/af-android-sdk
    implementation("com.appsflyer:af-android-sdk:6.17.2")

    implementation("com.geyifeng.immersionbar:immersionbar:3.2.2")
    implementation("com.geyifeng.immersionbar:immersionbar-ktx:3.2.2")

    implementation("com.google.android.ump:user-messaging-platform:3.2.0")

    implementation("com.google.android.play:review:2.0.2")
    implementation("com.google.android.play:review-ktx:2.0.2")

    // Lottie animation
    implementation("com.airbnb.android:lottie:6.4.0")
}
