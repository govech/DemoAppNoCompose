# 环境配置指南

本文档详细说明了如何搭建 Android MVVM 框架项目的开发环境。

## 📋 目录

- [系统要求](#系统要求)
- [必需工具安装](#必需工具安装)
- [项目配置](#项目配置)
- [环境验证](#环境验证)
- [常见问题](#常见问题)
- [高级配置](#高级配置)

## 💻 系统要求

### 硬件要求

#### 最低配置
- **内存**: 8GB RAM
- **存储**: 10GB 可用空间
- **处理器**: Intel i5 或 AMD 同等级别

#### 推荐配置
- **内存**: 16GB RAM 或更多
- **存储**: 20GB 可用空间 (SSD 推荐)
- **处理器**: Intel i7 或 AMD 同等级别

### 操作系统

#### 支持的系统
- **Windows**: Windows 10/11 (64-bit)
- **macOS**: macOS 10.14 或更高版本
- **Linux**: Ubuntu 18.04 LTS 或更高版本

## 🛠️ 必需工具安装

### 1. Java Development Kit (JDK)

#### 安装 JDK 17
```bash
# Windows (使用 Chocolatey)
choco install openjdk17

# macOS (使用 Homebrew)
brew install openjdk@17

# Ubuntu
sudo apt update
sudo apt install openjdk-17-jdk
```

#### 验证安装
```bash
java -version
javac -version
```

预期输出：
```
openjdk version "17.0.x" 2023-xx-xx
OpenJDK Runtime Environment (build 17.0.x+xx)
OpenJDK 64-Bit Server VM (build 17.0.x+xx, mixed mode, sharing)
```

#### 设置环境变量

**Windows:**
```cmd
setx JAVA_HOME "C:\Program Files\OpenJDK\openjdk-17"
setx PATH "%PATH%;%JAVA_HOME%\bin"
```

**macOS/Linux:**
```bash
# 添加到 ~/.bashrc 或 ~/.zshrc
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$PATH:$JAVA_HOME/bin
```

### 2. Android Studio

#### 下载安装
1. 访问 [Android Studio 官网](https://developer.android.com/studio)
2. 下载适合你操作系统的版本
3. 运行安装程序并按照向导完成安装

#### 首次启动配置
1. 选择 "Standard" 安装类型
2. 接受许可协议
3. 等待 SDK 组件下载完成

#### 必需的 SDK 组件
在 SDK Manager 中安装以下组件：

**SDK Platforms:**
- Android 14 (API 34) - 目标版本
- Android 13 (API 33)
- Android 6.0 (API 23) - 最低支持版本

**SDK Tools:**
- Android SDK Build-Tools 34.0.0
- Android Emulator
- Android SDK Platform-Tools
- Intel x86 Emulator Accelerator (HAXM installer)

### 3. Git

#### 安装 Git
```bash
# Windows (使用 Chocolatey)
choco install git

# macOS (使用 Homebrew)
brew install git

# Ubuntu
sudo apt update
sudo apt install git
```

#### 配置 Git
```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
git config --global init.defaultBranch main
```

### 4. Kotlin

Kotlin 通过 Android Studio 自动安装，无需单独安装。

## 📱 项目配置

### 1. 克隆项目

```bash
# 使用 HTTPS
git clone https://github.com/your-username/DemoAppNoCompose.git

# 或使用 SSH
git clone git@github.com:your-username/DemoAppNoCompose.git

# 进入项目目录
cd DemoAppNoCompose
```

### 2. 在 Android Studio 中打开项目

1. 启动 Android Studio
2. 选择 "Open an Existing Project"
3. 导航到项目目录并选择
4. 等待 Gradle 同步完成

### 3. 配置项目设置

#### 修改包名
1. 在 `app/build.gradle.kts` 中修改：
```kotlin
android {
    namespace = "com.yourcompany.yourapp"
    
    defaultConfig {
        applicationId = "com.yourcompany.yourapp"
        // ...
    }
}
```

2. 重构包名：
   - 右键点击包名 `lj.sword.demoappnocompose`
   - 选择 "Refactor" → "Rename"
   - 输入新的包名

#### 修改应用名称
在 `app/src/main/res/values/strings.xml` 中：
```xml
<string name="app_name">Your App Name</string>
```

#### 配置 API 地址
在 `app/build.gradle.kts` 中修改各环境的 BASE_URL：
```kotlin
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"https://api-dev.yourcompany.com/\"")
        buildConfigField("String", "ENVIRONMENT", "\"debug\"")
    }
    
    create("staging") {
        buildConfigField("String", "BASE_URL", "\"https://api-staging.yourcompany.com/\"")
        buildConfigField("String", "ENVIRONMENT", "\"staging\"")
    }
    
    release {
        buildConfigField("String", "BASE_URL", "\"https://api.yourcompany.com/\"")
        buildConfigField("String", "ENVIRONMENT", "\"release\"")
    }
}
```

### 4. 同步项目

```bash
# 在项目根目录执行
./gradlew build
```

或在 Android Studio 中点击 "Sync Project with Gradle Files"。

## ✅ 环境验证

### 1. 编译项目

```bash
# 编译 Debug 版本
./gradlew assembleDebug

# 编译 Release 版本
./gradlew assembleRelease
```

### 2. 运行测试

```bash
# 运行单元测试
./gradlew test

# 运行 Android 测试
./gradlew connectedAndroidTest
```

### 3. 代码质量检查

```bash
# 运行 ktlint 检查
./gradlew ktlintCheck

# 运行 detekt 分析
./gradlew detekt

# 运行所有质量检查
./scripts/code-quality.sh  # Linux/macOS
./scripts/code-quality.bat # Windows
```

### 4. 创建虚拟设备

1. 在 Android Studio 中打开 AVD Manager
2. 点击 "Create Virtual Device"
3. 选择设备型号（推荐 Pixel 6）
4. 选择系统镜像（推荐 API 34）
5. 完成创建并启动模拟器

### 5. 运行应用

```bash
# 安装到连接的设备/模拟器
./gradlew installDebug

# 或在 Android Studio 中点击运行按钮
```

## ❓ 常见问题

### 1. Gradle 同步失败

#### 问题：网络连接问题
```
Could not resolve all artifacts for configuration ':classpath'
```

**解决方案：**
```bash
# 使用阿里云镜像（中国用户）
# 在 build.gradle.kts 中添加：
repositories {
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    maven { url = uri("https://maven.aliyun.com/repository/central") }
    google()
    mavenCentral()
}
```

#### 问题：JDK 版本不匹配
```
Unsupported Java. Your build is currently configured to use Java 11.0.x
```

**解决方案：**
1. 确保安装了 JDK 17
2. 在 Android Studio 中设置：
   - File → Project Structure → SDK Location
   - 设置 JDK location 为 JDK 17 路径

### 2. 模拟器启动失败

#### 问题：HAXM 未安装
```
emulator: ERROR: x86 emulation currently requires hardware acceleration!
```

**解决方案：**
```bash
# Windows
# 在 SDK Manager 中安装 Intel x86 Emulator Accelerator (HAXM installer)
# 然后运行：%ANDROID_HOME%\extras\intel\Hardware_Accelerated_Execution_Manager\intelhaxm-android.exe

# macOS
# 确保启用了 Hypervisor.framework
sudo xcode-select --install

# Linux
# 确保启用了 KVM
sudo apt install qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils
```

### 3. 编译错误

#### 问题：依赖版本冲突
```
Duplicate class found in modules
```

**解决方案：**
```bash
# 清理项目
./gradlew clean

# 删除 .gradle 目录
rm -rf .gradle

# 重新同步
./gradlew build
```

#### 问题：内存不足
```
Expiring Daemon because JVM heap space is exhausted
```

**解决方案：**
在 `gradle.properties` 中增加内存配置：
```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
```

### 4. 网络请求失败

#### 问题：HTTP 明文传输被阻止
```
CLEARTEXT communication not permitted
```

**解决方案：**
在 `AndroidManifest.xml` 中添加（仅用于开发环境）：
```xml
<application
    android:usesCleartextTraffic="true"
    ... >
```

## 🚀 高级配置

### 1. 性能优化

#### Gradle 配置优化
在 `gradle.properties` 中添加：
```properties
# 启用并行编译
org.gradle.parallel=true

# 启用配置缓存
org.gradle.configuration-cache=true

# 启用构建缓存
org.gradle.caching=true

# 启用 Kotlin 增量编译
kotlin.incremental=true

# 启用 Kotlin 并行编译
kotlin.parallel.tasks.in.project=true
```

#### Android Studio 优化
1. File → Settings → Build, Execution, Deployment → Compiler
2. 增加 "Build process heap size" 到 2048 MB
3. 启用 "Compile independent modules in parallel"

### 2. 代码模板配置

#### 创建 Activity 模板
1. File → Settings → Editor → File and Code Templates
2. 创建新模板 "Android Activity"：

```kotlin
#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ${PACKAGE_NAME}.base.BaseActivity
import ${PACKAGE_NAME}.databinding.Activity${NAME}Binding

@AndroidEntryPoint
class ${NAME}Activity : BaseActivity<Activity${NAME}Binding, ${NAME}ViewModel>() {

    override val viewModel: ${NAME}ViewModel by viewModels()

    override fun createBinding(): Activity${NAME}Binding {
        return Activity${NAME}Binding.inflate(layoutInflater)
    }

    override fun initView() {
        // TODO: Initialize views
    }

    override fun initData() {
        // TODO: Load data
    }

    override fun setListeners() {
        // TODO: Set listeners
    }
}
```

### 3. 调试配置

#### 网络调试
在 `NetworkModule` 中添加调试拦截器：
```kotlin
@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                addNetworkInterceptor(StethoInterceptor())
            }
        }
        .build()
}
```

#### 数据库调试
添加 Stetho 依赖（仅 Debug 版本）：
```kotlin
debugImplementation("com.facebook.stetho:stetho:1.6.0")
debugImplementation("com.facebook.stetho:stetho-okhttp3:1.6.0")
```

### 4. 持续集成准备

#### GitHub Actions 配置
创建 `.github/workflows/ci.yml`：
```yaml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Run lint
      run: ./gradlew ktlintCheck
    
    - name: Run detekt
      run: ./gradlew detekt
```

## 📚 参考资源

- [Android Studio 用户指南](https://developer.android.com/studio/intro)
- [Gradle 用户手册](https://docs.gradle.org/current/userguide/userguide.html)
- [Kotlin 官方文档](https://kotlinlang.org/docs/)
- [Android 开发者指南](https://developer.android.com/guide)

## 🆘 获取帮助

如果在环境配置过程中遇到问题：

1. **查看日志**: 仔细阅读错误信息和日志
2. **搜索文档**: 在官方文档中搜索相关问题
3. **社区求助**: 在 Stack Overflow 或 GitHub Issues 中提问
4. **团队协作**: 向团队成员寻求帮助

---

*完成环境配置后，你就可以开始愉快的 Android 开发之旅了！* 🎉