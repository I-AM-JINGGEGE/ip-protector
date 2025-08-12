// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.github.ben-manes.versions") version "0.51.0"
    id("com.google.devtools.ksp") version "2.0.21-1.0.26" apply false
}

buildscript {
    apply(from = "repositories.gradle.kts")
    rootProject.extra.apply {
        set("applicationId", "com.ironmeta.ip.hider")
        set("compileSdkVersion", 35)
        set("buildToolsVersion", "34.0.0")
        set("minSdkVersion", 26)
        set("targetSdkVersion", 34)

        set("javaVersion", JavaVersion.VERSION_17)
        set("coroutinesVersion", "1.9.0")
        set("lifecycleVersion", "2.8.7")
        set("firebaseBomVersion", "33.9.0")
    }

    repositories {
        maven("https://www.jitpack.io")
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    dependencies {
        val androidPlugin: String by rootProject.extra
        val kotlinVersion: String by rootProject.extra

        classpath(androidPlugin)
        classpath(kotlin("gradle-plugin", kotlinVersion))
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.2")

        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.30.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")
        classpath("org.mozilla.rust-android-gradle:plugin:0.9.4")
    }
}

allprojects {
    apply(from = "${rootProject.projectDir}/repositories.gradle.kts")
    repositories {
        mavenCentral()
        maven ("https://maven.aliyun.com/repository/public")
        maven ("https://www.jitpack.io")
        google()
        maven ("https://plugins.gradle.org/m2/")
    }
}

// skip uploading the mapping to Crashlytics
subprojects {
    tasks.whenTaskAdded {
        if (name.contains("uploadCrashlyticsMappingFile")) enabled = false
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
