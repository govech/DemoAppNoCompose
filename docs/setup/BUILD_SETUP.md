# 构建配置指南

本文档详细说明了 Android MVVM 框架项目的构建配置和优化策略。

## 📋 目录

- [构建系统概述](#构建系统概述)
- [Gradle 配置](#gradle-配置)
- [构建类型配置](#构建类型配置)
- [依赖管理](#依赖管理)
- [代码混淆配置](#代码混淆配置)
- [构建优化](#构建优化)
- [多模块配置](#多模块配置)
- [构建脚本](#构建脚本)

## 🎯 构建系统概述

### 构建架构

```
┌─────────────────────────────────────────┐
│              Root Project               │
├─────────────────────────────────────────┤
│ ├── app (Android Application)           │
│ ├── core (Android Library)              │
│ ├── data (Android Library)              │
│ ├── domain (Kotlin Library)             │
│ └── buildSrc (Build Logic)              │
└─────────────────────────────────────────┘
```

### 技术栈

- **构建工具**: Gradle 8.0+
- **Android Gradle Plugin**: 8.1.0+
- **Kotlin**: 1.9.0+
- **Java**: 17
- **构建缓存**: 启用
- **并行构建**: 启用

## ⚙️ Gradle 配置

### 根项目配置

```kotlin
// build.gradle.kts (Project)
buildscript {
    extra.apply {
        set("kotlin_version", "1.9.0")
        set("compose_version", "1.5.0")
        set("hilt_version", "2.47")
        set("room_version", "2.5.0")
        set("retrofit_version", "2.9.0")
        set("lifecycle_version", "2.7.0")
    }
}

plugins {
    id("com.android.application") version "8.1.0" apply false
    id("com.android.library") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.47" apply false
    id("androidx.room") version "2.5.0" apply false
    id("kotlin-parcelize") apply false
    id("kotlin-kapt") apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.0" apply false
    id("org.sonarqube") version "4.2.1" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
    id("com.google.firebase.crashlytics") version "2.9.7" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
```

### Gradle 属性配置

```properties
# gradle.properties
# Kotlin
kotlin.code.style=official

# Android
android.useAndroidX=true
android.enableJetifier=true

# Gradle
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true

# Kotlin Compiler
kotlin.incremental=true
kotlin.incremental.android=true
kotlin.incremental.js=true

# Android Build
android.enableR8.fullMode=true
android.enableBuildCache=true
android.experimental.enableSourceSetPathsMap=true

# Kapt
kapt.incremental.apt=true
kapt.use.worker.api=true
kapt.include.compile.classpath=false

# Build Features
android.defaults.buildfeatures.buildconfig=true
android.defaults.buildfeatures.aidl=false
android.defaults.buildfeatures.renderscript=false
android.defaults.buildfeatures.resvalues=false
android.defaults.buildfeatures.shaders=false

# Performance
org.gradle.unsafe.configuration-cache=true
org.gradle.configuration-cache.problems=warn
```

### 版本目录配置

```kotlin
// gradle/libs.versions.toml
[versions]
kotlin = "1.9.0"
compose = "1.5.0"
compose-compiler = "1.5.0"
compose-bom = "2023.08.00"
hilt = "2.47"
room = "2.5.0"
retrofit = "2.9.0"
okhttp = "4.11.0"
lifecycle = "2.7.0"
navigation = "2.7.0"
coroutines = "1.7.3"
junit = "4.13.2"
junit-ext = "1.1.5"
espresso = "3.5.1"
mockito = "5.4.0"
truth = "1.1.5"
detekt = "1.23.0"

[libraries]
# Kotlin
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }

# Android
androidx-core-ktx = "androidx.core:core-ktx:1.10.1"
androidx-lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-ktx = { module = "androidx.lifecycle:lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
androidx-lifecycle-livedata-ktx = { module = "androidx.lifecycle:lifecycle-livedata-ktx", version.ref = "lifecycle" }

# Compose
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-activity = "androidx.activity:activity-compose:1.7.2"
compose-navigation = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }

# Hilt
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = "androidx.hilt:hilt-navigation-compose:1.0.0"

# Room
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }

# Network
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
retrofit-gson = { module = "com.squareup.retrofit2:converter-gson", version.ref = "retrofit" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okhttp-logging = { module = "com.squareup.okhttp3:logging-interceptor", version.ref = "okhttp" }

# Testing
junit = { module = "junit:junit", version.ref = "junit" }
junit-ext = { module = "androidx.test.ext:junit", version.ref = "junit-ext" }
espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "espresso" }
mockito-core = { module = "org.mockito:mockito-core", version.ref = "mockito" }
mockito-kotlin = "org.mockito.kotlin:mockito-kotlin:5.0.0"
truth = { module = "com.google.truth:truth", version.ref = "truth" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
compose-ui-test = { module = "androidx.compose.ui:ui-test-junit4" }
compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }

[bundles]
compose = ["compose-ui", "compose-ui-tooling-preview", "compose-material3", "compose-activity", "compose-navigation"]
lifecycle = ["androidx-lifecycle-runtime-ktx", "androidx-lifecycle-viewmodel-ktx", "androidx-lifecycle-livedata-ktx"]
room = ["room-runtime", "room-ktx"]
retrofit = ["retrofit", "retrofit-gson", "okhttp", "okhttp-logging"]
testing = ["junit", "mockito-core", "mockito-kotlin", "truth", "coroutines-test"]
android-testing = ["junit-ext", "espresso-core"]

[plugins]
android-application = { id = "com.android.application", version = "8.1.0" }
android-library = { id = "com.android.library", version = "8.1.0" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
kapt = { id = "kotlin-kapt" }
parcelize = { id = "kotlin-parcelize" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

## 🏗️ 构建类型配置

### 应用模块配置

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kapt)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.yourapp.demoappnocompose"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.yourapp.demoappnocompose"
        minSdk = 24
        targetSdk = 34
        versionCode = getVersionCode()
        versionName = getVersionName()
        
        testInstrumentationRunner = "com.yourapp.demoappnocompose.HiltTestRunner"
        vectorDrawables.useSupportLibrary = true
        
        // 构建配置字段
        buildConfigField("String", "BUILD_TIME", "\"${getBuildTime()}\"")
        buildConfigField("String", "GIT_COMMIT", "\"${getGitCommit()}\"")
        buildConfigField("boolean", "ENABLE_LOGGING", "true")
    }
    
    signingConfigs {
        create("release") {
            storeFile = file(findProperty("RELEASE_STORE_FILE") ?: "release.keystore")
            storePassword = findProperty("RELEASE_STORE_PASSWORD") as String?
            keyAlias = findProperty("RELEASE_KEY_ALIAS") as String?
            keyPassword = findProperty("RELEASE_KEY_PASSWORD") as String?
        }
    }
    
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            buildConfigField("String", "API_BASE_URL", "\"https://api-dev.yourapp.com\"")
            buildConfigField("boolean", "ENABLE_CRASH_REPORTING", "false")
            
            // 测试覆盖率
            isTestCoverageEnabled = true
        }
        
        create("staging") {
            initWith(getByName("debug"))
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            buildConfigField("String", "API_BASE_URL", "\"https://api-staging.yourapp.com\"")
            buildConfigField("boolean", "ENABLE_CRASH_REPORTING", "true")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
        }
        
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            buildConfigField("String", "API_BASE_URL", "\"https://api.yourapp.com\"")
            buildConfigField("boolean", "ENABLE_CRASH_REPORTING", "true")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
        }
    }
    
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            
            buildConfigField("String", "FLAVOR", "\"dev\"")
        }
        
        create("prod") {
            dimension = "environment"
            
            buildConfigField("String", "FLAVOR", "\"prod\"")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
        dataBinding = false
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        
        animationsDisabled = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
    
    lint {
        abortOnError = false
        checkReleaseBuilds = true
        ignoreWarnings = false
        warningsAsErrors = false
        
        disable += setOf(
            "MissingTranslation",
            "ExtraTranslation"
        )
    }
}

dependencies {
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.room)
    implementation(libs.bundles.retrofit)
    
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)
    kapt(libs.room.compiler)
    
    // Core library desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
    
    // Testing
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.android.testing)
    androidTestImplementation(libs.compose.ui.test)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)
    
    // Test orchestrator
    androidTestUtil("androidx.test:orchestrator:1.4.2")
    
    // Detekt
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.0")
}

// 辅助函数
fun getVersionCode(): Int {
    return findProperty("VERSION_CODE")?.toString()?.toIntOrNull() ?: 1
}

fun getVersionName(): String {
    return findProperty("VERSION_NAME")?.toString() ?: "1.0.0"
}

fun getBuildTime(): String {
    return java.time.Instant.now().toString()
}

fun getGitCommit(): String {
    return try {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .directory(rootDir)
            .start()
        process.inputStream.bufferedReader().readText().trim()
    } catch (e: Exception) {
        "unknown"
    }
}
```

## 📦 依赖管理

### 依赖版本管理策略

```kotlin
// buildSrc/src/main/kotlin/Dependencies.kt
object Dependencies {
    
    object Versions {
        const val kotlin = "1.9.0"
        const val compose = "1.5.0"
        const val hilt = "2.47"
        const val room = "2.5.0"
        const val retrofit = "2.9.0"
        const val lifecycle = "2.7.0"
    }
    
    object AndroidX {
        const val core = "androidx.core:core-ktx:1.10.1"
        const val appcompat = "androidx.appcompat:appcompat:1.6.1"
        const val material = "com.google.android.material:material:1.9.0"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
        
        object Lifecycle {
            const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
            const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
        }
        
        object Compose {
            const val bom = "androidx.compose:compose-bom:2023.08.00"
            const val ui = "androidx.compose.ui:ui"
            const val tooling = "androidx.compose.ui:ui-tooling"
            const val preview = "androidx.compose.ui:ui-tooling-preview"
            const val material3 = "androidx.compose.material3:material3"
            const val activity = "androidx.activity:activity-compose:1.7.2"
            const val navigation = "androidx.navigation:navigation-compose:2.7.0"
        }
        
        object Room {
            const val runtime = "androidx.room:room-runtime:${Versions.room}"
            const val compiler = "androidx.room:room-compiler:${Versions.room}"
            const val ktx = "androidx.room:room-ktx:${Versions.room}"
        }
    }
    
    object Google {
        object Hilt {
            const val android = "com.google.dagger:hilt-android:${Versions.hilt}"
            const val compiler = "com.google.dagger:hilt-compiler:${Versions.hilt}"
            const val navigationCompose = "androidx.hilt:hilt-navigation-compose:1.0.0"
        }
    }
    
    object Network {
        const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
        const val gsonConverter = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
        const val okhttp = "com.squareup.okhttp3:okhttp:4.11.0"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:4.11.0"
    }
    
    object Testing {
        const val junit = "junit:junit:4.13.2"
        const val junitExt = "androidx.test.ext:junit:1.1.5"
        const val espresso = "androidx.test.espresso:espresso-core:3.5.1"
        const val mockito = "org.mockito:mockito-core:5.4.0"
        const val mockitoKotlin = "org.mockito.kotlin:mockito-kotlin:5.0.0"
        const val truth = "com.google.truth:truth:1.1.5"
        const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"
        const val composeTest = "androidx.compose.ui:ui-test-junit4"
        const val composeTestManifest = "androidx.compose.ui:ui-test-manifest"
    }
}
```

### 依赖更新策略

```kotlin
// buildSrc/src/main/kotlin/DependencyUpdates.kt
tasks.named("dependencyUpdates").configure {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
```

## 🔒 代码混淆配置

### ProGuard 规则

```proguard
# proguard-rules.pro

# Keep application class
-keep public class * extends android.app.Application

# Keep all model classes
-keep class com.yourapp.demoappnocompose.data.model.** { *; }
-keep class com.yourapp.demoappnocompose.domain.model.** { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-dontwarn com.google.dagger.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
```

### R8 配置

```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        release {
            // 启用 R8 完整模式
            isMinifyEnabled = true
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

## ⚡ 构建优化

### Gradle 性能优化

```properties
# gradle.properties

# 增加 Gradle 堆内存
org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC

# 启用并行构建
org.gradle.parallel=true

# 启用构建缓存
org.gradle.caching=true

# 启用配置缓存
org.gradle.configuration-cache=true

# 启用按需配置
org.gradle.configureondemand=true

# Kotlin 增量编译
kotlin.incremental=true
kotlin.incremental.android=true

# Kapt 优化
kapt.incremental.apt=true
kapt.use.worker.api=true
kapt.include.compile.classpath=false

# Android 构建优化
android.enableR8.fullMode=true
android.enableBuildCache=true
android.experimental.enableSourceSetPathsMap=true
```

### 构建时间分析

```bash
#!/bin/bash
# scripts/analyze-build.sh

echo "🔍 Analyzing build performance..."

# 生成构建扫描
./gradlew build --scan

# 分析构建时间
./gradlew assembleDebug --profile

# 分析依赖
./gradlew app:dependencies > dependencies.txt

# 分析任务执行时间
./gradlew assembleDebug --dry-run

echo "📊 Build analysis completed!"
echo "Check build/reports/profile/ for detailed reports"
```

### 构建缓存配置

```kotlin
// settings.gradle.kts
buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
    
    remote<HttpBuildCache> {
        url = uri("https://your-build-cache-server.com/cache/")
        credentials {
            username = System.getenv("BUILD_CACHE_USERNAME")
            password = System.getenv("BUILD_CACHE_PASSWORD")
        }
        isPush = System.getenv("CI") != null
    }
}
```

## 🏢 多模块配置

### 模块结构

```
project/
├── app/                    # Android Application
├── core/                   # Core Android Library
│   ├── common/            # Common utilities
│   ├── network/           # Network layer
│   ├── database/          # Database layer
│   └── ui/                # UI components
├── feature/               # Feature modules
│   ├── login/             # Login feature
│   ├── profile/           # Profile feature
│   └── settings/          # Settings feature
├── data/                  # Data layer
└── domain/                # Domain layer (Pure Kotlin)
```

### 共享构建逻辑

```kotlin
// buildSrc/src/main/kotlin/AndroidLibraryConventionPlugin.kt
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("kotlin-kapt")
            }
            
            extensions.configure<LibraryExtension> {
                compileSdk = 34
                
                defaultConfig {
                    minSdk = 24
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                
                kotlinOptions {
                    jvmTarget = "17"
                }
                
                buildFeatures {
                    compose = true
                }
                
                composeOptions {
                    kotlinCompilerExtensionVersion = "1.5.0"
                }
            }
            
            dependencies {
                add("implementation", libs.findLibrary("androidx-core-ktx").get())
                add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())
                
                add("testImplementation", libs.findLibrary("junit").get())
                add("androidTestImplementation", libs.findLibrary("junit-ext").get())
                add("androidTestImplementation", libs.findLibrary("espresso-core").get())
            }
        }
    }
}
```

### 功能模块配置

```kotlin
// feature/login/build.gradle.kts
plugins {
    id("android-library-convention")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.yourapp.demoappnocompose.feature.login"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":domain"))
    
    implementation(libs.bundles.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)
}
```

## 🛠️ 构建脚本

### 完整构建脚本

```bash
#!/bin/bash
# scripts/build-all.sh

set -e

echo "🏗️ Starting complete build process..."

# 环境检查
echo "🔍 Checking environment..."
if ! command -v java &> /dev/null; then
    echo "❌ Java not found"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher required, found Java $JAVA_VERSION"
    exit 1
fi

# 清理
echo "🧹 Cleaning project..."
./gradlew clean

# 代码检查
echo "🔍 Running code analysis..."
./gradlew detekt lint

# 单元测试
echo "🧪 Running unit tests..."
./gradlew testDebugUnitTest

# 构建所有变体
echo "📦 Building all variants..."
./gradlew assembleDebug
./gradlew assembleStaging
./gradlew assembleRelease

# 生成测试报告
echo "📊 Generating reports..."
./gradlew jacocoTestReport

# 构建 AAB
echo "📦 Building AAB..."
./gradlew bundleRelease

echo "✅ Build completed successfully!"
echo "📁 Outputs:"
echo "  - APK Debug: app/build/outputs/apk/debug/"
echo "  - APK Staging: app/build/outputs/apk/staging/"
echo "  - APK Release: app/build/outputs/apk/release/"
echo "  - AAB Release: app/build/outputs/bundle/release/"
echo "  - Reports: app/build/reports/"
```

### 快速开发构建

```bash
#!/bin/bash
# scripts/build-dev.sh

set -e

echo "🚀 Quick development build..."

# 只构建 debug 版本
./gradlew assembleDebug

# 安装到连接的设备
if adb devices | grep -q "device$"; then
    echo "📱 Installing to device..."
    ./gradlew installDebug
    
    # 启动应用
    adb shell am start -n com.yourapp.demoappnocompose.debug/.MainActivity
else
    echo "⚠️ No devices connected"
fi

echo "✅ Development build completed!"
```

### 发布构建脚本

```bash
#!/bin/bash
# scripts/build-release.sh

set -e

echo "🚀 Building release version..."

# 检查签名配置
if [ -z "$RELEASE_STORE_PASSWORD" ]; then
    echo "❌ RELEASE_STORE_PASSWORD not set"
    exit 1
fi

# 完整测试
echo "🧪 Running full test suite..."
./gradlew test
./gradlew connectedAndroidTest

# 代码质量检查
echo "🔍 Running quality checks..."
./gradlew detekt lint

# 构建发布版本
echo "📦 Building release..."
./gradlew assembleRelease bundleRelease

# 验证签名
echo "✅ Verifying signatures..."
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

echo "🎉 Release build completed successfully!"
```

---

*良好的构建配置是项目成功的基础，能够显著提高开发效率和代码质量。*