plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

android {
    namespace = "lj.sword.demoappnocompose"
    compileSdk = 36

    defaultConfig {
        applicationId = "lj.sword.demoappnocompose"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Room schema export
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    // 多环境配置
    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            buildConfigField("String", "BASE_URL", "\"https://api-dev.example.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"DEV\"")
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"PRODUCTION\"")
        }
        
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            
            buildConfigField("String", "BASE_URL", "\"https://api-test.example.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"TEST\"")
        }
    }
    
    // ViewBinding
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview"
        )
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        htmlReport = true
        xmlReport = true
        htmlOutput = file("${layout.buildDirectory.get().asFile}/reports/lint-results.html")
        xmlOutput = file("${layout.buildDirectory.get().asFile}/reports/lint-results.xml")
    }
}

dependencies {

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.common.java8)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines & Flow
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)

    // Image Loading
    implementation(libs.coil)

    // Local Storage
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // SmartRefreshLayout
    implementation(libs.smartrefresh.layout)
    implementation(libs.smartrefresh.header.classics)
    implementation(libs.smartrefresh.footer.classics)

    // LeakCanary (Debug only)
    debugImplementation(libs.leakcanary.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// 代码质量工具配置

// ktlint 配置
ktlint {
    version.set("1.0.1")
    debug.set(true)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.SARIF)
    }
    
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

// detekt 配置
detekt {
    toolVersion = "1.23.4"
    config.setFrom(file("${rootProject.projectDir}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

// 自定义任务：代码质量检查
tasks.register("codeQualityCheck") {
    dependsOn("ktlintCheck", "detekt", "lint")
    group = "verification"
    description = "运行所有代码质量检查"
}

// 自定义任务：代码格式化
tasks.register("codeFormat") {
    dependsOn("ktlintFormat")
    group = "formatting"
    description = "格式化所有Kotlin代码"
}

// 在build之前运行代码质量检查
tasks.named("build") {
    dependsOn("codeQualityCheck")
}