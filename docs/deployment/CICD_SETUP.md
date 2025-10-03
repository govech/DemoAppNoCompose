# CI/CD 配置指南

本文档详细说明了 Android MVVM 框架的持续集成和持续部署配置。

## 📋 目录

- [CI/CD 概述](#cicd-概述)
- [GitHub Actions 配置](#github-actions-配置)
- [构建流水线](#构建流水线)
- [测试自动化](#测试自动化)
- [代码质量检查](#代码质量检查)
- [部署策略](#部署策略)
- [监控和通知](#监控和通知)

## 🎯 CI/CD 概述

### CI/CD 目标

- **自动化构建**: 代码提交后自动触发构建
- **自动化测试**: 运行单元测试、集成测试和UI测试
- **代码质量**: 自动进行代码质量检查和安全扫描
- **自动化部署**: 自动部署到测试环境和生产环境

### 流水线架构

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   代码提交   │───▶│   构建阶段   │───▶│   测试阶段   │───▶│   部署阶段   │
│             │    │             │    │             │    │             │
│ Git Push    │    │ Compile     │    │ Unit Tests  │    │ Dev Deploy  │
│ Pull Request│    │ Lint Check  │    │ UI Tests    │    │ Prod Deploy │
│             │    │ Build APK   │    │ Security    │    │ Release     │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

## 🔧 GitHub Actions 配置

### 主要工作流配置

```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]
  release:
    types: [ published ]

env:
  JAVA_VERSION: 17
  ANDROID_API_LEVEL: 34
  ANDROID_BUILD_TOOLS_VERSION: 34.0.0

jobs:
  lint:
    name: Lint Check
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK
      uses: actions/setup-java@v4
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: 'temurin'
        
    - name: Cache Gradle dependencies
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
          
    - name: Make gradlew executable
      run: chmod +x ./gradlew
      
    - name: Run lint
      run: ./gradlew lintDebug
      
    - name: Upload lint reports
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: lint-reports
        path: app/build/reports/lint-results-debug.html

  unit-tests:
    name: Unit Tests
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK
      uses: actions/setup-java@v4
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: 'temurin'
        
    - name: Cache Gradle dependencies
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
          
    - name: Make gradlew executable
      run: chmod +x ./gradlew
      
    - name: Run unit tests
      run: ./gradlew testDebugUnitTest
      
    - name: Generate test coverage report
      run: ./gradlew jacocoTestReport
      
    - name: Upload test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: unit-test-results
        path: app/build/test-results/testDebugUnitTest/
        
    - name: Upload coverage reports
      uses: codecov/codecov-action@v3
      with:
        file: app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
        flags: unittests
        name: codecov-umbrella

  instrumented-tests:
    name: Instrumented Tests
    runs-on: macos-latest
    
    strategy:
      matrix:
        api-level: [28, 30, 34]
        
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK
      uses: actions/setup-java@v4
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: 'temurin'
        
    - name: Cache Gradle dependencies
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
          
    - name: Make gradlew executable
      run: chmod +x ./gradlew
      
    - name: AVD cache
      uses: actions/cache@v3
      id: avd-cache
      with:
        path: |
          ~/.android/avd/*
          ~/.android/adb*
        key: avd-${{ matrix.api-level }}
        
    - name: Create AVD and generate snapshot for caching
      if: steps.avd-cache.outputs.cache-hit != 'true'
      uses: reactivecircus/android-emulator-runner@v2
      with:
        api-level: ${{ matrix.api-level }}
        force-avd-creation: false
        emulator-options: -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -camera-back none
        disable-animations: false
        script: echo "Generated AVD snapshot for caching."
        
    - name: Run instrumented tests
      uses: reactivecircus/android-emulator-runner@v2
      with:
        api-level: ${{ matrix.api-level }}
        force-avd-creation: false
        emulator-options: -no-snapshot-save -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -camera-back none
        disable-animations: true
        script: ./gradlew connectedDebugAndroidTest
        
    - name: Upload test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: instrumented-test-results-api-${{ matrix.api-level }}
        path: app/build/reports/androidTests/connected/

  build:
    name: Build APK
    runs-on: ubuntu-latest
    needs: [lint, unit-tests]
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK
      uses: actions/setup-java@v4
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: 'temurin'
        
    - name: Cache Gradle dependencies
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
          
    - name: Make gradlew executable
      run: chmod +x ./gradlew
      
    - name: Build debug APK
      run: ./gradlew assembleDebug
      
    - name: Upload debug APK
      uses: actions/upload-artifact@v3
      with:
        name: debug-apk
        path: app/build/outputs/apk/debug/app-debug.apk

  security-scan:
    name: Security Scan
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Run Snyk to check for vulnerabilities
      uses: snyk/actions/gradle@master
      env:
        SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
      with:
        args: --severity-threshold=high
        
    - name: Upload Snyk results
      uses: github/codeql-action/upload-sarif@v2
      if: always()
      with:
        sarif_file: snyk.sarif

  deploy-staging:
    name: Deploy to Staging
    runs-on: ubuntu-latest
    needs: [build, instrumented-tests]
    if: github.ref == 'refs/heads/develop'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Download APK
      uses: actions/download-artifact@v3
      with:
        name: debug-apk
        path: ./
        
    - name: Deploy to Firebase App Distribution
      uses: wzieba/Firebase-Distribution-Github-Action@v1
      with:
        appId: ${{ secrets.FIREBASE_APP_ID }}
        serviceCredentialsFileContent: ${{ secrets.CREDENTIAL_FILE_CONTENT }}
        groups: testers
        file: app-debug.apk
        releaseNotes: "Automated build from develop branch"

  deploy-production:
    name: Deploy to Production
    runs-on: ubuntu-latest
    needs: [build, instrumented-tests, security-scan]
    if: github.event_name == 'release'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK
      uses: actions/setup-java@v4
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: 'temurin'
        
    - name: Decode keystore
      run: |
        echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > keystore.jks
        
    - name: Build release APK
      run: ./gradlew assembleRelease
      env:
        KEYSTORE_FILE: ../keystore.jks
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        
    - name: Build release AAB
      run: ./gradlew bundleRelease
      env:
        KEYSTORE_FILE: ../keystore.jks
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        
    - name: Upload to Google Play
      uses: r0adkll/upload-google-play@v1
      with:
        serviceAccountJsonPlainText: ${{ secrets.GOOGLE_PLAY_SERVICE_ACCOUNT }}
        packageName: com.yourapp.demoappnocompose
        releaseFiles: app/build/outputs/bundle/release/app-release.aab
        track: production
        status: completed
```

### 分支保护规则

```yaml
# .github/workflows/branch-protection.yml
name: Branch Protection

on:
  pull_request:
    branches: [ main ]

jobs:
  enforce-branch-protection:
    name: Enforce Branch Protection
    runs-on: ubuntu-latest
    
    steps:
    - name: Check PR requirements
      run: |
        echo "Checking PR requirements..."
        
        # 检查 PR 标题格式
        if [[ ! "${{ github.event.pull_request.title }}" =~ ^(feat|fix|docs|style|refactor|test|chore)(\(.+\))?: .+ ]]; then
          echo "PR title must follow conventional commit format"
          exit 1
        fi
        
        # 检查 PR 描述
        if [[ -z "${{ github.event.pull_request.body }}" ]]; then
          echo "PR description is required"
          exit 1
        fi
        
        echo "PR requirements check passed"
```

## 🏗️ 构建流水线

### Gradle 构建配置

```kotlin
// build.gradle.kts (app module)
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.yourapp.demoappnocompose"
        minSdk 24
        targetSdk 34
        versionCode getVersionCode()
        versionName getVersionName()
        
        testInstrumentationRunner "com.yourapp.demoappnocompose.HiltTestRunner"
        
        buildConfigField("String", "BUILD_TIME", "\"${getBuildTime()}\"")
        buildConfigField("String", "GIT_COMMIT", "\"${getGitCommit()}\"")
    }
    
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            buildConfigField("String", "API_BASE_URL", "\"https://api-dev.yourapp.com\"")
        }
        
        create("staging") {
            initWith(getByName("debug"))
            isDebuggable = false
            isMinifyEnabled = true
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "API_BASE_URL", "\"https://api-staging.yourapp.com\"")
        }
        
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "API_BASE_URL", "\"https://api.yourapp.com\"")
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
}

fun getVersionCode(): Int {
    val versionCode = System.getenv("VERSION_CODE")?.toIntOrNull()
    return versionCode ?: 1
}

fun getVersionName(): String {
    val versionName = System.getenv("VERSION_NAME")
    return versionName ?: "1.0.0"
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

### 构建脚本

```bash
#!/bin/bash
# scripts/build.sh

set -e

echo "🏗️ Starting build process..."

# 环境变量检查
if [ -z "$VERSION_NAME" ]; then
    echo "❌ VERSION_NAME environment variable is required"
    exit 1
fi

if [ -z "$VERSION_CODE" ]; then
    echo "❌ VERSION_CODE environment variable is required"
    exit 1
fi

# 清理构建目录
echo "🧹 Cleaning build directory..."
./gradlew clean

# 运行代码检查
echo "🔍 Running code analysis..."
./gradlew lintDebug detekt

# 运行单元测试
echo "🧪 Running unit tests..."
./gradlew testDebugUnitTest

# 生成测试覆盖率报告
echo "📊 Generating coverage report..."
./gradlew jacocoTestReport

# 构建 APK
echo "📦 Building APK..."
./gradlew assembleRelease

# 构建 AAB
echo "📦 Building AAB..."
./gradlew bundleRelease

echo "✅ Build completed successfully!"
echo "📁 APK location: app/build/outputs/apk/release/app-release.apk"
echo "📁 AAB location: app/build/outputs/bundle/release/app-release.aab"
```

## 🧪 测试自动化

### 测试配置

```kotlin
// HiltTestRunner.kt
class HiltTestRunner : AndroidJUnitRunner() {
    
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}

@HiltAndroidApp
class HiltTestApplication : Application()
```

### 测试脚本

```bash
#!/bin/bash
# scripts/run-tests.sh

set -e

echo "🧪 Running automated tests..."

# 启动模拟器（如果需要）
if [ "$RUN_INSTRUMENTED_TESTS" = "true" ]; then
    echo "📱 Starting emulator..."
    $ANDROID_HOME/emulator/emulator -avd test_avd -no-window -no-audio &
    
    # 等待模拟器启动
    echo "⏳ Waiting for emulator to start..."
    adb wait-for-device
    adb shell input keyevent 82  # 解锁屏幕
fi

# 运行单元测试
echo "🔬 Running unit tests..."
./gradlew testDebugUnitTest --continue

# 运行集成测试（如果启用）
if [ "$RUN_INSTRUMENTED_TESTS" = "true" ]; then
    echo "🔗 Running instrumented tests..."
    ./gradlew connectedDebugAndroidTest --continue
fi

# 生成测试报告
echo "📊 Generating test reports..."
./gradlew jacocoTestReport

# 检查测试覆盖率
echo "📈 Checking test coverage..."
./gradlew jacocoTestCoverageVerification

echo "✅ All tests completed!"
```

## 🔍 代码质量检查

### Detekt 配置

```yaml
# detekt.yml
build:
  maxIssues: 0
  excludeCorrectable: false
  weights:
    complexity: 2
    LongParameterList: 1
    style: 1
    comments: 1

config:
  validation: true
  warningsAsErrors: false
  checkExhaustiveness: false

processors:
  active: true
  exclude:
    - 'DetektProgressListener'

console-reports:
  active: true
  exclude:
    - 'ProjectStatisticsReport'
    - 'ComplexityReport'
    - 'NotificationReport'
    - 'FindingsReport'
    - 'FileBasedFindingsReport'

output-reports:
  active: true
  exclude:
    - 'TxtOutputReport'
    - 'XmlOutputReport'
    - 'HtmlOutputReport'

comments:
  active: true
  CommentOverPrivateFunction:
    active: false
  CommentOverPrivateProperty:
    active: false
  EndOfSentenceFormat:
    active: false
    endOfSentenceFormat: '([.?!][ \t\n\r\f<])|([.?!:]$)'
  UndocumentedPublicClass:
    active: false
    searchInNestedClass: true
    searchInInnerClass: true
    searchInInnerObject: true
    searchInInnerInterface: true
  UndocumentedPublicFunction:
    active: false

complexity:
  active: true
  ComplexCondition:
    active: true
    threshold: 4
  ComplexInterface:
    active: false
    threshold: 10
    includeStaticDeclarations: false
    includePrivateDeclarations: false
  ComplexMethod:
    active: true
    threshold: 15
    ignoreSingleWhenExpression: false
    ignoreSimpleWhenEntries: false
    ignoreNestingFunctions: false
    nestingFunctions: [run, let, apply, with, also, use, forEach, isNotNull, ifNull]
  LabeledExpression:
    active: false
    ignoredLabels: []
  LargeClass:
    active: true
    threshold: 600
  LongMethod:
    active: true
    threshold: 60
  LongParameterList:
    active: true
    functionThreshold: 6
    constructorThreshold: 7
    ignoreDefaultParameters: false
    ignoreDataClasses: true
    ignoreAnnotated: []
  MethodOverloading:
    active: false
    threshold: 6
  NestedBlockDepth:
    active: true
    threshold: 4
  StringLiteralDuplication:
    active: false
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    threshold: 3
    ignoreAnnotation: true
    excludeStringsWithLessThan5Characters: true
    ignoreStringsRegex: '$^'
  TooManyFunctions:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    thresholdInFiles: 11
    thresholdInClasses: 11
    thresholdInInterfaces: 11
    thresholdInObjects: 11
    thresholdInEnums: 11
    ignoreDeprecated: false
    ignorePrivate: false
    ignoreOverridden: false

coroutines:
  active: true
  GlobalCoroutineUsage:
    active: false
  RedundantSuspendModifier:
    active: false
  SleepInsteadOfDelay:
    active: true
  SuspendFunWithFlowReturnType:
    active: true

empty-blocks:
  active: true
  EmptyCatchBlock:
    active: true
    allowedExceptionNameRegex: "^(_|(ignore|expected).*)"
  EmptyClassBlock:
    active: true
  EmptyDefaultConstructor:
    active: true
  EmptyDoWhileBlock:
    active: true
  EmptyElseBlock:
    active: true
  EmptyFinallyBlock:
    active: true
  EmptyForBlock:
    active: true
  EmptyFunctionBlock:
    active: true
    ignoreOverridden: false
  EmptyIfBlock:
    active: true
  EmptyInitBlock:
    active: true
  EmptyKtFile:
    active: true
  EmptySecondaryConstructor:
    active: true
  EmptyTryBlock:
    active: true
  EmptyWhenBlock:
    active: true
  EmptyWhileBlock:
    active: true

exceptions:
  active: true
  ExceptionRaisedInUnexpectedLocation:
    active: false
    methodNames: [toString, hashCode, equals, finalize]
  InstanceOfCheckForException:
    active: false
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
  NotImplementedDeclaration:
    active: false
  PrintStackTrace:
    active: false
  RethrowCaughtException:
    active: false
  ReturnFromFinally:
    active: false
    ignoreLabeled: false
  SwallowedException:
    active: false
    ignoredExceptionTypes:
      - InterruptedException
      - NumberFormatException
      - ParseException
      - MalformedURLException
    allowedExceptionNameRegex: "^(_|(ignore|expected).*)"
  ThrowingExceptionFromFinally:
    active: false
  ThrowingExceptionInMain:
    active: false
  ThrowingExceptionsWithoutMessageOrCause:
    active: false
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    exceptions:
      - IllegalArgumentException
      - IllegalStateException
      - IOException
  ThrowingNewInstanceOfSameException:
    active: false
  TooGenericExceptionCaught:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    exceptionNames:
      - ArrayIndexOutOfBoundsException
      - Error
      - Exception
      - IllegalMonitorStateException
      - NullPointerException
      - IndexOutOfBoundsException
      - RuntimeException
      - Throwable
    allowedExceptionNameRegex: "^(_|(ignore|expected).*)"
  TooGenericExceptionThrown:
    active: true
    exceptionNames:
      - Error
      - Exception
      - Throwable
      - RuntimeException

formatting:
  active: true
  android: false
  autoCorrect: true
  AnnotationOnSeparateLine:
    active: false
    autoCorrect: true
  AnnotationSpacing:
    active: false
    autoCorrect: true
  ArgumentListWrapping:
    active: false
    autoCorrect: true
  ChainWrapping:
    active: true
    autoCorrect: true
  CommentSpacing:
    active: true
    autoCorrect: true
  EnumEntryNameCase:
    active: false
    autoCorrect: true
  Filename:
    active: true
  FinalNewline:
    active: true
    autoCorrect: true
    insertFinalNewLine: true
  ImportOrdering:
    active: false
    autoCorrect: true
    layout: '*,java.**,javax.**,kotlin.**,^'
  Indentation:
    active: false
    autoCorrect: true
    indentSize: 4
    continuationIndentSize: 4
  MaximumLineLength:
    active: true
    maxLineLength: 120
  ModifierOrdering:
    active: true
    autoCorrect: true
  MultiLineIfElse:
    active: false
    autoCorrect: true
  NoBlankLineBeforeRbrace:
    active: true
    autoCorrect: true
  NoConsecutiveBlankLines:
    active: true
    autoCorrect: true
  NoEmptyClassBody:
    active: true
    autoCorrect: true
  NoEmptyFirstLineInMethodBlock:
    active: false
    autoCorrect: true
  NoLineBreakAfterElse:
    active: true
    autoCorrect: true
  NoLineBreakBeforeAssignment:
    active: true
    autoCorrect: true
  NoMultipleSpaces:
    active: true
    autoCorrect: true
  NoSemicolons:
    active: true
    autoCorrect: true
  NoTrailingSpaces:
    active: true
    autoCorrect: true
  NoUnitReturn:
    active: true
    autoCorrect: true
  NoUnusedImports:
    active: true
    autoCorrect: true
  NoWildcardImports:
    active: true
  PackageName:
    active: true
    autoCorrect: true
  ParameterListWrapping:
    active: false
    autoCorrect: true
    indentSize: 4
  SpacingAroundColon:
    active: true
    autoCorrect: true
  SpacingAroundComma:
    active: true
    autoCorrect: true
  SpacingAroundCurly:
    active: true
    autoCorrect: true
  SpacingAroundDot:
    active: true
    autoCorrect: true
  SpacingAroundDoubleColon:
    active: false
    autoCorrect: true
  SpacingAroundKeyword:
    active: true
    autoCorrect: true
  SpacingAroundOperators:
    active: true
    autoCorrect: true
  SpacingAroundParens:
    active: true
    autoCorrect: true
  SpacingAroundRangeOperator:
    active: true
    autoCorrect: true
  SpacingAroundUnaryOperator:
    active: false
    autoCorrect: true
  SpacingBetweenDeclarationsWithAnnotations:
    active: false
    autoCorrect: true
  SpacingBetweenDeclarationsWithComments:
    active: false
    autoCorrect: true
  StringTemplate:
    active: true
    autoCorrect: true

naming:
  active: true
  ClassNaming:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    classPattern: '[A-Z][a-zA-Z0-9]*'
  ConstructorParameterNaming:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    parameterPattern: '[a-z][A-Za-z0-9]*'
    privateParameterPattern: '[a-z][A-Za-z0-9]*'
    excludeClassPattern: '$^'
    ignoreOverridden: true
  EnumNaming:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    enumEntryPattern: '[A-Z][_a-zA-Z0-9]*'
  ForbiddenClassName:
    active: false
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    forbiddenName: []
  FunctionMaxLength:
    active: false
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    maximumFunctionNameLength: 30
  FunctionMinLength:
    active: false
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    minimumFunctionNameLength: 3
  FunctionNaming:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    functionPattern: '([a-z][a-zA-Z0-9]*)|(`.*`)'
    excludeClassPattern: '$^'
    ignoreOverridden: true
    ignoreAnnotated: ['Composable']
  FunctionParameterNaming:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    parameterPattern: '[a-z][A-Za-z0-9]*'
    excludeClassPattern: '$^'
    ignoreOverridden: true
  InvalidPackageDeclaration:
    active: false
    rootPackage: ''
  MatchingDeclarationName:
    active: true
    mustBeFirst: true
  MemberNameEqualsClassName:
    active: true
    ignoreOverridden: true
  ObjectPropertyNaming:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    constantPattern: '[A-Za-z][_A-Za-z0-9]*'
    propertyPattern: '[A-Za-z][_A-Za-z0-9]*'
    privatePropertyPattern: '(_)?[A-Za-z][_A-Za-z0-9]*'
  PackageNaming:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    packagePattern: '[a-z]+(\.[a-z][A-Za-z0-9]*)*'
  TopLevelPropertyNaming:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    constantPattern: '[A-Z][_A-Z0-9]*'
    propertyPattern: '[A-Za-z][_A-Za-z0-9]*'
    privatePropertyPattern: '_?[A-Za-z][_A-Za-z0-9]*'
  VariableMaxLength:
    active: false
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    maximumVariableNameLength: 64
  VariableMinLength:
    active: false
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    minimumVariableNameLength: 1
  VariableNaming:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    variablePattern: '[a-z][A-Za-z0-9]*'
    privateVariablePattern: '(_)?[a-z][A-Za-z0-9]*'
    excludeClassPattern: '$^'
    ignoreOverridden: true

performance:
  active: true
  ArrayPrimitive:
    active: true
  ForEachOnRange:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
  SpreadOperator:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
  UnnecessaryTemporaryInstantiation:
    active: true

potential-bugs:
  active: true
  Deprecation:
    active: false
  DuplicateCaseInWhenExpression:
    active: true
  EqualsAlwaysReturnsTrueOrFalse:
    active: true
  EqualsWithHashCodeExist:
    active: true
  ExplicitGarbageCollectionCall:
    active: true
  HasPlatformType:
    active: false
  IgnoredReturnValue:
    active: false
    restrictToAnnotatedMethods: true
    returnValueAnnotations: ['*.CheckReturnValue', '*.CheckResult']
  ImplicitDefaultLocale:
    active: false
  ImplicitUnitReturnType:
    active: false
    allowExplicitReturnType: true
  InvalidRange:
    active: true
  IteratorHasNextCallsNextMethod:
    active: true
  IteratorNotThrowingNoSuchElementException:
    active: true
  LateinitUsage:
    active: false
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    excludeAnnotatedProperties: []
    ignoreOnClassesPattern: ''
  MapGetWithNotNullAssertionOperator:
    active: false
  MissingWhenCase:
    active: true
    allowElseExpression: true
  NullableToStringCall:
    active: false
  RedundantElseInWhen:
    active: true
  UnconditionalJumpStatementInLoop:
    active: false
  UnnecessaryNotNullOperator:
    active: false
  UnnecessarySafeCall:
    active: false
  UnreachableCode:
    active: true
  UnsafeCallOnNullableType:
    active: true
  UnsafeCast:
    active: false
  UselessPostfixExpression:
    active: false
  WrongEqualsTypeParameter:
    active: true

style:
  active: true
  ClassOrdering:
    active: false
  CollapsibleIfStatements:
    active: false
  DataClassContainsFunction:
    active: false
  DataClassShouldBeImmutable:
    active: false
  DestructuringDeclarationWithTooManyEntries:
    active: false
    maxDestructuringEntries: 3
  EqualsNullCall:
    active: true
  EqualsOnSignatureLine:
    active: false
  ExplicitCollectionElementAccessMethod:
    active: false
  ExplicitItLambdaParameter:
    active: false
  ExpressionBodySyntax:
    active: false
    includeLineWrapping: false
  ForbiddenComment:
    active: true
    values: ['TODO:', 'FIXME:', 'STOPSHIP:']
    allowedPatterns: ''
  ForbiddenImport:
    active: false
    imports: []
    forbiddenPatterns: ''
  ForbiddenMethodCall:
    active: false
    methods: ['kotlin.io.println', 'kotlin.io.print']
  ForbiddenPublicDataClass:
    active: false
    excludes: ['**']
    ignorePackages: ['*.internal', '*.internal.*']
  ForbiddenVoid:
    active: false
    ignoreOverridden: false
    ignoreUsageInGenerics: false
  FunctionOnlyReturningConstant:
    active: true
    ignoreOverridableFunction: true
    excludedFunctions: 'describeContents'
    excludeAnnotatedFunction: ['dagger.Provides']
  LibraryCodeMustSpecifyReturnType:
    active: false
    excludes: ['**']
  LibraryEntitiesShouldNotBePublic:
    active: false
    excludes: ['**']
  LoopWithTooManyJumpStatements:
    active: true
    maxJumpCount: 1
  MagicNumber:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    ignoreNumbers: ['-1', '0', '1', '2']
    ignoreHashCodeFunction: true
    ignorePropertyDeclaration: false
    ignoreLocalVariableDeclaration: false
    ignoreConstantDeclaration: true
    ignoreCompanionObjectPropertyDeclaration: true
    ignoreAnnotation: false
    ignoreNamedArgument: true
    ignoreEnums: false
    ignoreRanges: false
    ignoreExtensionFunctions: true
  MandatoryBracesIfStatements:
    active: false
  MandatoryBracesLoops:
    active: false
  MaxLineLength:
    active: true
    maxLineLength: 120
    excludePackageStatements: true
    excludeImportStatements: true
    excludeCommentStatements: false
  MayBeConst:
    active: true
  ModifierOrder:
    active: true
  NestedClassesVisibility:
    active: false
  NewLineAtEndOfFile:
    active: true
  NoTabs:
    active: false
  OptionalAbstractKeyword:
    active: true
  OptionalUnit:
    active: false
  OptionalWhenBraces:
    active: false
  PreferToOverPairSyntax:
    active: false
  ProtectedMemberInFinalClass:
    active: true
  RedundantExplicitType:
    active: false
  RedundantHigherOrderMapUsage:
    active: false
  RedundantVisibilityModifierRule:
    active: false
  ReturnCount:
    active: true
    max: 2
    excludedFunctions: 'equals'
    excludeLabeled: false
    excludeReturnFromLambda: true
    excludeGuardClauses: false
  SafeCast:
    active: true
  SerialVersionUIDInSerializableClass:
    active: false
  SpacingBetweenPackageAndImports:
    active: false
  ThrowsCount:
    active: true
    max: 2
    excludeGuardClauses: false
  TrailingWhitespace:
    active: false
  UnderscoresInNumericLiterals:
    active: false
    acceptableDecimalLength: 5
  UnnecessaryAbstractClass:
    active: true
    excludeAnnotatedClasses: ['dagger.Module']
  UnnecessaryAnnotationUseSiteTarget:
    active: false
  UnnecessaryApply:
    active: false
  UnnecessaryFilter:
    active: false
  UnnecessaryInheritance:
    active: true
  UnnecessaryLet:
    active: false
  UnnecessaryParentheses:
    active: false
  UntilInsteadOfRangeTo:
    active: false
  UnusedImports:
    active: false
  UnusedPrivateClass:
    active: true
  UnusedPrivateMember:
    active: false
    allowedNames: '(_|ignored|expected|serialVersionUID)'
  UseArrayLiteralsInAnnotations:
    active: false
  UseCheckOrError:
    active: false
  UseDataClass:
    active: false
    excludeAnnotatedClasses: []
    allowVars: false
  UseEmptyCounterpart:
    active: false
  UseIfEmptyOrIfBlank:
    active: false
  UseIfInsteadOfWhen:
    active: false
  UseIsNullOrEmpty:
    active: false
  UseOrEmpty:
    active: false
  UseRequire:
    active: false
  UseRequireNotNull:
    active: false
  UselessCallOnNotNull:
    active: true
  UtilityClassWithPublicConstructor:
    active: true
  VarCouldBeVal:
    active: true
  WildcardImport:
    active: true
    excludes: ['**/test/**', '**/androidTest/**', '**/commonTest/**', '**/jvmTest/**', '**/jsTest/**', '**/iosTest/**']
    excludeImports: ['java.util.*', 'kotlinx.android.synthetic.*']
```

### SonarQube 集成

```yaml
# .github/workflows/sonarqube.yml
name: SonarQube Analysis

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  sonarqube:
    name: SonarQube Analysis
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      with:
        fetch-depth: 0
        
    - name: Set up JDK
      uses: actions/setup-java@v4
      with:
        java-version: 17
        distribution: 'temurin'
        
    - name: Cache SonarQube packages
      uses: actions/cache@v3
      with:
        path: ~/.sonar/cache
        key: ${{ runner.os }}-sonar
        restore-keys: ${{ runner.os }}-sonar
        
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: ~/.gradle/caches
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
        restore-keys: ${{ runner.os }}-gradle
        
    - name: Run tests and generate coverage
      run: ./gradlew testDebugUnitTest jacocoTestReport
      
    - name: SonarQube Scan
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
      run: ./gradlew sonarqube --info
```

## 🚀 部署策略

### 环境配置

```kotlin
// BuildConfig.kt
object BuildConfig {
    const val DEBUG = true
    const val APPLICATION_ID = "com.yourapp.demoappnocompose"
    const val BUILD_TYPE = "debug"
    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0.0"
    
    // 环境相关配置
    const val API_BASE_URL = "https://api-dev.yourapp.com"
    const val ENABLE_LOGGING = true
    const val ENABLE_CRASH_REPORTING = false
}
```

### 部署脚本

```bash
#!/bin/bash
# scripts/deploy.sh

set -e

ENVIRONMENT=${1:-staging}
VERSION_NAME=${2:-$(git describe --tags --abbrev=0)}
VERSION_CODE=${3:-$(git rev-list --count HEAD)}

echo "🚀 Deploying to $ENVIRONMENT..."
echo "📦 Version: $VERSION_NAME ($VERSION_CODE)"

case $ENVIRONMENT in
  "staging")
    echo "📱 Building staging APK..."
    ./gradlew assembleStaging
    
    echo "🔥 Uploading to Firebase App Distribution..."
    firebase appdistribution:distribute \
      app/build/outputs/apk/staging/app-staging.apk \
      --app $FIREBASE_APP_ID_STAGING \
      --groups "internal-testers" \
      --release-notes "Staging build $VERSION_NAME"
    ;;
    
  "production")
    echo "📱 Building production AAB..."
    ./gradlew bundleRelease
    
    echo "🏪 Uploading to Google Play Console..."
    fastlane supply \
      --aab app/build/outputs/bundle/release/app-release.aab \
      --track production \
      --release_status completed
    ;;
    
  *)
    echo "❌ Unknown environment: $ENVIRONMENT"
    echo "Usage: $0 [staging|production] [version_name] [version_code]"
    exit 1
    ;;
esac

echo "✅ Deployment to $ENVIRONMENT completed!"
```

## 📊 监控和通知

### Slack 通知配置

```yaml
# .github/workflows/notifications.yml
name: Build Notifications

on:
  workflow_run:
    workflows: ["CI/CD Pipeline"]
    types:
      - completed

jobs:
  notify:
    name: Send Notifications
    runs-on: ubuntu-latest
    
    steps:
    - name: Notify Slack on Success
      if: ${{ github.event.workflow_run.conclusion == 'success' }}
      uses: 8398a7/action-slack@v3
      with:
        status: success
        channel: '#android-builds'
        text: |
          ✅ Build succeeded for ${{ github.event.workflow_run.head_branch }}
          Commit: ${{ github.event.workflow_run.head_sha }}
          Author: ${{ github.event.workflow_run.head_commit.author.name }}
      env:
        SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
        
    - name: Notify Slack on Failure
      if: ${{ github.event.workflow_run.conclusion == 'failure' }}
      uses: 8398a7/action-slack@v3
      with:
        status: failure
        channel: '#android-builds'
        text: |
          ❌ Build failed for ${{ github.event.workflow_run.head_branch }}
          Commit: ${{ github.event.workflow_run.head_sha }}
          Author: ${{ github.event.workflow_run.head_commit.author.name }}
          Please check the logs: ${{ github.event.workflow_run.html_url }}
      env:
        SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

### 构建状态监控

```kotlin
// BuildStatusReporter.kt
class BuildStatusReporter {
    
    fun reportBuildStart(buildInfo: BuildInfo) {
        val message = """
            🏗️ Build Started
            Branch: ${buildInfo.branch}
            Commit: ${buildInfo.commitHash}
            Author: ${buildInfo.author}
            Build Number: ${buildInfo.buildNumber}
        """.trimIndent()
        
        sendToSlack(message)
        sendToTeams(message)
    }
    
    fun reportBuildSuccess(buildInfo: BuildInfo, artifacts: List<Artifact>) {
        val message = """
            ✅ Build Successful
            Branch: ${buildInfo.branch}
            Version: ${buildInfo.versionName} (${buildInfo.versionCode})
            Duration: ${buildInfo.duration}
            Artifacts: ${artifacts.joinToString { it.name }}
        """.trimIndent()
        
        sendToSlack(message)
        sendToTeams(message)
    }
    
    fun reportBuildFailure(buildInfo: BuildInfo, error: String) {
        val message = """
            ❌ Build Failed
            Branch: ${buildInfo.branch}
            Error: $error
            Logs: ${buildInfo.logsUrl}
        """.trimIndent()
        
        sendToSlack(message, urgent = true)
        sendToTeams(message)
    }
    
    private fun sendToSlack(message: String, urgent: Boolean = false) {
        // Slack webhook implementation
    }
    
    private fun sendToTeams(message: String) {
        // Teams webhook implementation
    }
}
```

---

*CI/CD 是现代软件开发的重要组成部分，能够显著提高开发效率和代码质量。*