# 发布流程指南

本文档详细说明了 Android MVVM 框架的应用发布流程和最佳实践。

## 📋 目录

- [发布流程概述](#发布流程概述)
- [版本管理](#版本管理)
- [发布前准备](#发布前准备)
- [构建和签名](#构建和签名)
- [测试验证](#测试验证)
- [应用商店发布](#应用商店发布)
- [发布后监控](#发布后监控)

## 🎯 发布流程概述

### 发布类型

- **Alpha 版本**: 内部测试版本，功能可能不完整
- **Beta 版本**: 公开测试版本，功能基本完整
- **Release 版本**: 正式发布版本，经过完整测试

### 发布流程图

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  开发完成    │───▶│  版本准备    │───▶│  构建测试    │───▶│  正式发布    │
│             │    │             │    │             │    │             │
│ Feature     │    │ Version     │    │ Build &     │    │ Store       │
│ Complete    │    │ Bump        │    │ Test        │    │ Release     │
│             │    │ Changelog   │    │ Sign        │    │ Monitor     │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

## 📊 版本管理

### 版本号规范

采用语义化版本控制 (Semantic Versioning)：

```
MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]

例如：
- 1.0.0        (正式版本)
- 1.1.0-beta.1 (Beta 版本)
- 1.0.1-alpha  (Alpha 版本)
- 2.0.0-rc.1   (Release Candidate)
```

### 版本号规则

- **MAJOR**: 不兼容的 API 修改
- **MINOR**: 向下兼容的功能性新增
- **PATCH**: 向下兼容的问题修正
- **PRERELEASE**: 预发布版本标识
- **BUILD**: 构建元数据

### 版本管理脚本

```bash
#!/bin/bash
# scripts/version-bump.sh

set -e

CURRENT_VERSION=$(grep "versionName" app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')
echo "Current version: $CURRENT_VERSION"

# 解析版本号
IFS='.' read -ra VERSION_PARTS <<< "$CURRENT_VERSION"
MAJOR=${VERSION_PARTS[0]}
MINOR=${VERSION_PARTS[1]}
PATCH=${VERSION_PARTS[2]}

case $1 in
  "major")
    NEW_MAJOR=$((MAJOR + 1))
    NEW_VERSION="$NEW_MAJOR.0.0"
    ;;
  "minor")
    NEW_MINOR=$((MINOR + 1))
    NEW_VERSION="$MAJOR.$NEW_MINOR.0"
    ;;
  "patch")
    NEW_PATCH=$((PATCH + 1))
    NEW_VERSION="$MAJOR.$MINOR.$NEW_PATCH"
    ;;
  "beta")
    NEW_VERSION="$CURRENT_VERSION-beta.$(date +%Y%m%d)"
    ;;
  "alpha")
    NEW_VERSION="$CURRENT_VERSION-alpha.$(date +%Y%m%d)"
    ;;
  *)
    echo "Usage: $0 [major|minor|patch|beta|alpha]"
    exit 1
    ;;
esac

echo "New version: $NEW_VERSION"

# 更新版本号
sed -i "s/versionName \"$CURRENT_VERSION\"/versionName \"$NEW_VERSION\"/" app/build.gradle.kts

# 更新版本代码
CURRENT_CODE=$(grep "versionCode" app/build.gradle.kts | sed 's/.*\([0-9]\+\).*/\1/')
NEW_CODE=$((CURRENT_CODE + 1))
sed -i "s/versionCode $CURRENT_CODE/versionCode $NEW_CODE/" app/build.gradle.kts

echo "Updated version to $NEW_VERSION (code: $NEW_CODE)"

# 提交版本更新
git add app/build.gradle.kts
git commit -m "chore: bump version to $NEW_VERSION"
git tag -a "v$NEW_VERSION" -m "Release version $NEW_VERSION"

echo "Version bump completed!"
```

## 🔧 发布前准备

### 1. 代码审查清单

```markdown
## 代码审查清单

### 功能完整性
- [ ] 所有计划功能已实现
- [ ] 所有已知 bug 已修复
- [ ] 新功能已经过充分测试
- [ ] 性能优化已完成

### 代码质量
- [ ] 代码审查已通过
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试已通过
- [ ] UI 测试已通过
- [ ] 静态代码分析无严重问题

### 文档更新
- [ ] README 已更新
- [ ] CHANGELOG 已更新
- [ ] API 文档已更新
- [ ] 用户指南已更新

### 安全检查
- [ ] 安全扫描已通过
- [ ] 敏感信息已移除
- [ ] 权限声明已审查
- [ ] 第三方库安全性已确认

### 兼容性测试
- [ ] 目标 Android 版本测试
- [ ] 不同设备尺寸测试
- [ ] 不同分辨率测试
- [ ] 性能基准测试
```

### 2. 更新日志生成

```bash
#!/bin/bash
# scripts/generate-changelog.sh

set -e

LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

if [ -z "$LAST_TAG" ]; then
    echo "No previous tag found, generating changelog from first commit"
    COMMIT_RANGE=""
else
    echo "Generating changelog from $LAST_TAG to HEAD"
    COMMIT_RANGE="$LAST_TAG..HEAD"
fi

# 生成更新日志
cat > CHANGELOG_TEMP.md << EOF
# Changelog

## [Unreleased]

### Added
$(git log $COMMIT_RANGE --pretty=format:"- %s" --grep="^feat" | sed 's/^feat[(:]//')

### Changed
$(git log $COMMIT_RANGE --pretty=format:"- %s" --grep="^refactor\|^perf" | sed 's/^refactor[(:]//' | sed 's/^perf[(:]//')

### Fixed
$(git log $COMMIT_RANGE --pretty=format:"- %s" --grep="^fix" | sed 's/^fix[(:]//')

### Security
$(git log $COMMIT_RANGE --pretty=format:"- %s" --grep="^security" | sed 's/^security[(:]//')

EOF

echo "Changelog generated in CHANGELOG_TEMP.md"
echo "Please review and merge with existing CHANGELOG.md"
```

### 3. 发布分支创建

```bash
#!/bin/bash
# scripts/create-release-branch.sh

set -e

VERSION=$1
if [ -z "$VERSION" ]; then
    echo "Usage: $0 <version>"
    echo "Example: $0 1.2.0"
    exit 1
fi

RELEASE_BRANCH="release/$VERSION"

echo "Creating release branch: $RELEASE_BRANCH"

# 确保在 develop 分支
git checkout develop
git pull origin develop

# 创建发布分支
git checkout -b $RELEASE_BRANCH

# 更新版本号
./scripts/version-bump.sh $VERSION

# 推送发布分支
git push -u origin $RELEASE_BRANCH

echo "Release branch $RELEASE_BRANCH created successfully!"
echo "Next steps:"
echo "1. Complete final testing"
echo "2. Update documentation"
echo "3. Create pull request to main"
echo "4. Tag and release"
```

## 🏗️ 构建和签名

### 1. 签名配置

```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file(project.findProperty("RELEASE_STORE_FILE") ?: "release.keystore")
            storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String?
            keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String?
            keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String?
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 2. 密钥管理

```properties
# gradle.properties (本地文件，不提交到版本控制)
RELEASE_STORE_FILE=../keystore/release.keystore
RELEASE_STORE_PASSWORD=your_store_password
RELEASE_KEY_ALIAS=your_key_alias
RELEASE_KEY_PASSWORD=your_key_password
```

### 3. 构建脚本

```bash
#!/bin/bash
# scripts/build-release.sh

set -e

echo "🏗️ Building release version..."

# 检查环境变量
if [ -z "$RELEASE_STORE_PASSWORD" ]; then
    echo "❌ RELEASE_STORE_PASSWORD not set"
    exit 1
fi

if [ -z "$RELEASE_KEY_PASSWORD" ]; then
    echo "❌ RELEASE_KEY_PASSWORD not set"
    exit 1
fi

# 清理构建目录
echo "🧹 Cleaning build directory..."
./gradlew clean

# 运行测试
echo "🧪 Running tests..."
./gradlew testReleaseUnitTest

# 运行代码检查
echo "🔍 Running code analysis..."
./gradlew lintRelease detekt

# 构建 APK
echo "📦 Building release APK..."
./gradlew assembleRelease

# 构建 AAB
echo "📦 Building release AAB..."
./gradlew bundleRelease

# 验证签名
echo "✅ Verifying signatures..."
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab

echo "✅ Release build completed successfully!"
echo "📁 APK: app/build/outputs/apk/release/app-release.apk"
echo "📁 AAB: app/build/outputs/bundle/release/app-release.aab"
```

## 🧪 测试验证

### 1. 发布前测试清单

```markdown
## 发布前测试清单

### 功能测试
- [ ] 核心功能正常工作
- [ ] 新功能按预期工作
- [ ] 回归测试通过
- [ ] 边界条件测试

### 性能测试
- [ ] 应用启动时间 < 3秒
- [ ] 内存使用正常
- [ ] CPU 使用率正常
- [ ] 网络请求响应时间正常

### 兼容性测试
- [ ] 最低支持版本测试
- [ ] 最新版本测试
- [ ] 不同屏幕尺寸测试
- [ ] 不同分辨率测试

### 安装测试
- [ ] 全新安装测试
- [ ] 升级安装测试
- [ ] 卸载测试
- [ ] 权限申请测试

### 网络测试
- [ ] 无网络环境测试
- [ ] 弱网络环境测试
- [ ] 网络切换测试
- [ ] 离线功能测试
```

### 2. 自动化测试脚本

```bash
#!/bin/bash
# scripts/run-release-tests.sh

set -e

echo "🧪 Running release tests..."

# 单元测试
echo "🔬 Running unit tests..."
./gradlew testReleaseUnitTest

# 集成测试
echo "🔗 Running integration tests..."
./gradlew testReleaseIntegrationTest

# UI 测试（如果有连接的设备）
if adb devices | grep -q "device$"; then
    echo "📱 Running UI tests..."
    ./gradlew connectedReleaseAndroidTest
else
    echo "⚠️ No devices connected, skipping UI tests"
fi

# 性能测试
echo "⚡ Running performance tests..."
./gradlew performanceTestRelease

# 生成测试报告
echo "📊 Generating test reports..."
./gradlew jacocoTestReport

echo "✅ All tests completed!"
```

## 🏪 应用商店发布

### 1. Google Play Console 发布

```bash
#!/bin/bash
# scripts/deploy-playstore.sh

set -e

AAB_PATH="app/build/outputs/bundle/release/app-release.aab"
TRACK=${1:-internal}  # internal, alpha, beta, production

echo "🏪 Deploying to Google Play Console..."
echo "📦 AAB: $AAB_PATH"
echo "🎯 Track: $TRACK"

# 检查 AAB 文件是否存在
if [ ! -f "$AAB_PATH" ]; then
    echo "❌ AAB file not found: $AAB_PATH"
    echo "Please run build-release.sh first"
    exit 1
fi

# 使用 Fastlane 上传
fastlane supply \
    --aab "$AAB_PATH" \
    --track "$TRACK" \
    --release_status "completed" \
    --skip_upload_metadata \
    --skip_upload_images \
    --skip_upload_screenshots

echo "✅ Successfully deployed to $TRACK track!"
```

### 2. Firebase App Distribution

```bash
#!/bin/bash
# scripts/deploy-firebase.sh

set -e

APK_PATH="app/build/outputs/apk/release/app-release.apk"
GROUPS=${1:-"internal-testers"}
RELEASE_NOTES=${2:-"Release build"}

echo "🔥 Deploying to Firebase App Distribution..."

firebase appdistribution:distribute "$APK_PATH" \
    --app "$FIREBASE_APP_ID" \
    --groups "$GROUPS" \
    --release-notes "$RELEASE_NOTES"

echo "✅ Successfully deployed to Firebase App Distribution!"
```

### 3. 发布配置文件

```yaml
# fastlane/Appfile
app_identifier("com.yourapp.demoappnocompose")
apple_id("your-apple-id@example.com")
itc_team_id("123456789")
team_id("ABCDEFGHIJ")

# fastlane/Fastfile
default_platform(:android)

platform :android do
  desc "Deploy to Google Play Internal Testing"
  lane :internal do
    gradle(task: "bundleRelease")
    upload_to_play_store(
      track: "internal",
      aab: "app/build/outputs/bundle/release/app-release.aab"
    )
  end

  desc "Deploy to Google Play Alpha"
  lane :alpha do
    gradle(task: "bundleRelease")
    upload_to_play_store(
      track: "alpha",
      aab: "app/build/outputs/bundle/release/app-release.aab"
    )
  end

  desc "Deploy to Google Play Beta"
  lane :beta do
    gradle(task: "bundleRelease")
    upload_to_play_store(
      track: "beta",
      aab: "app/build/outputs/bundle/release/app-release.aab"
    )
  end

  desc "Deploy to Google Play Production"
  lane :production do
    gradle(task: "bundleRelease")
    upload_to_play_store(
      track: "production",
      aab: "app/build/outputs/bundle/release/app-release.aab"
    )
  end
end
```

## 📊 发布后监控

### 1. 监控指标

```kotlin
// ReleaseMonitor.kt
class ReleaseMonitor {
    
    fun trackReleaseMetrics(version: String) {
        // 崩溃率监控
        trackCrashRate(version)
        
        // 性能监控
        trackPerformanceMetrics(version)
        
        // 用户反馈监控
        trackUserFeedback(version)
        
        // 下载和安装监控
        trackInstallMetrics(version)
    }
    
    private fun trackCrashRate(version: String) {
        Firebase.crashlytics.setCustomKey("app_version", version)
        
        // 设置崩溃率阈值告警
        if (getCrashRate(version) > 0.01) { // 1%
            sendAlert("High crash rate detected for version $version")
        }
    }
    
    private fun trackPerformanceMetrics(version: String) {
        // 应用启动时间
        val startupTime = measureStartupTime()
        Firebase.performance.newTrace("app_startup_$version").apply {
            putMetric("startup_time_ms", startupTime)
            start()
            stop()
        }
        
        // 内存使用
        val memoryUsage = getMemoryUsage()
        Firebase.performance.newTrace("memory_usage_$version").apply {
            putMetric("memory_mb", memoryUsage)
            start()
            stop()
        }
    }
    
    private fun trackUserFeedback(version: String) {
        // 应用商店评分监控
        val rating = getPlayStoreRating()
        if (rating < 4.0) {
            sendAlert("Low app store rating: $rating for version $version")
        }
        
        // 用户反馈分析
        analyzeUserFeedback(version)
    }
    
    private fun trackInstallMetrics(version: String) {
        // 下载量监控
        val downloads = getDownloadCount(version)
        
        // 安装成功率监控
        val installSuccessRate = getInstallSuccessRate(version)
        if (installSuccessRate < 0.95) { // 95%
            sendAlert("Low install success rate: $installSuccessRate for version $version")
        }
    }
}
```

### 2. 告警配置

```yaml
# monitoring/alerts.yml
alerts:
  - name: high_crash_rate
    condition: crash_rate > 0.01
    severity: critical
    channels: [slack, email]
    
  - name: low_app_rating
    condition: app_rating < 4.0
    severity: warning
    channels: [slack]
    
  - name: performance_degradation
    condition: startup_time > 5000
    severity: warning
    channels: [slack]
    
  - name: low_install_success_rate
    condition: install_success_rate < 0.95
    severity: critical
    channels: [slack, email]
```

### 3. 发布后检查清单

```markdown
## 发布后检查清单

### 立即检查 (发布后 1 小时)
- [ ] 应用在商店中可见
- [ ] 下载和安装正常
- [ ] 核心功能正常工作
- [ ] 崩溃率正常 (< 1%)

### 短期监控 (发布后 24 小时)
- [ ] 用户反馈正常
- [ ] 性能指标正常
- [ ] 服务器负载正常
- [ ] 错误日志检查

### 中期监控 (发布后 1 周)
- [ ] 用户留存率
- [ ] 应用商店评分
- [ ] 功能使用统计
- [ ] 性能趋势分析

### 长期监控 (发布后 1 个月)
- [ ] 整体用户满意度
- [ ] 业务指标影响
- [ ] 技术债务评估
- [ ] 下一版本规划
```

## 🔄 回滚策略

### 1. 回滚决策标准

```markdown
## 回滚触发条件

### 立即回滚
- 崩溃率 > 5%
- 严重安全漏洞
- 核心功能完全不可用
- 数据丢失或损坏

### 考虑回滚
- 崩溃率 > 2%
- 性能严重下降 (> 50%)
- 用户投诉激增
- 重要功能异常

### 监控观察
- 崩溃率 1-2%
- 轻微性能下降
- 少量用户反馈
- 非核心功能问题
```

### 2. 回滚执行脚本

```bash
#!/bin/bash
# scripts/rollback.sh

set -e

ROLLBACK_VERSION=$1
REASON=$2

if [ -z "$ROLLBACK_VERSION" ]; then
    echo "Usage: $0 <rollback_version> <reason>"
    echo "Example: $0 1.1.0 'Critical crash issue'"
    exit 1
fi

echo "🔄 Initiating rollback to version $ROLLBACK_VERSION"
echo "📝 Reason: $REASON"

# 确认回滚
read -p "Are you sure you want to rollback? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Rollback cancelled"
    exit 1
fi

# 执行回滚
echo "📱 Rolling back Google Play Console..."
fastlane supply \
    --version_name "$ROLLBACK_VERSION" \
    --track "production" \
    --rollout "100"

# 通知团队
echo "📢 Notifying team..."
curl -X POST -H 'Content-type: application/json' \
    --data "{\"text\":\"🚨 ROLLBACK EXECUTED\\nVersion: $ROLLBACK_VERSION\\nReason: $REASON\"}" \
    "$SLACK_WEBHOOK_URL"

echo "✅ Rollback completed!"
```

---

*发布流程的规范化和自动化是确保应用质量和用户体验的关键。*