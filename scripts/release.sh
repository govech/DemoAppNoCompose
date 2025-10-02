#!/bin/bash

# 版本发布脚本
# 自动化版本管理和发布流程

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 显示帮助信息
show_help() {
    echo "版本发布脚本"
    echo ""
    echo "用法: $0 [选项] <版本类型>"
    echo ""
    echo "版本类型:"
    echo "  major    主版本号 (1.0.0 -> 2.0.0)"
    echo "  minor    次版本号 (1.0.0 -> 1.1.0)"
    echo "  patch    修订版本号 (1.0.0 -> 1.0.1)"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示帮助信息"
    echo "  -d, --dry-run  预览模式，不实际执行"
    echo "  -s, --skip-tests  跳过测试"
    echo ""
    echo "示例:"
    echo "  $0 patch              # 发布修订版本"
    echo "  $0 minor --dry-run    # 预览次版本发布"
    echo "  $0 major --skip-tests # 发布主版本，跳过测试"
}

# 解析命令行参数
DRY_RUN=false
SKIP_TESTS=false
VERSION_TYPE=""

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -d|--dry-run)
            DRY_RUN=true
            shift
            ;;
        -s|--skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        major|minor|patch)
            VERSION_TYPE=$1
            shift
            ;;
        *)
            echo -e "${RED}❌ 未知参数: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# 检查版本类型
if [ -z "$VERSION_TYPE" ]; then
    echo -e "${RED}❌ 错误: 请指定版本类型 (major|minor|patch)${NC}"
    show_help
    exit 1
fi

# 检查是否在项目根目录
if [ ! -f "gradlew" ]; then
    echo -e "${RED}❌ 错误: 请在项目根目录运行此脚本${NC}"
    exit 1
fi

# 检查Git状态
check_git_status() {
    echo -e "${BLUE}🔍 检查Git状态...${NC}"
    
    if ! git diff-index --quiet HEAD --; then
        echo -e "${RED}❌ 错误: 工作目录有未提交的更改${NC}"
        echo "请先提交或暂存所有更改"
        exit 1
    fi
    
    CURRENT_BRANCH=$(git branch --show-current)
    if [ "$CURRENT_BRANCH" != "main" ] && [ "$CURRENT_BRANCH" != "master" ]; then
        echo -e "${YELLOW}⚠️  警告: 当前不在主分支 ($CURRENT_BRANCH)${NC}"
        read -p "是否继续? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# 获取当前版本
get_current_version() {
    # 从 build.gradle.kts 中提取版本号
    CURRENT_VERSION=$(grep 'versionName = ' app/build.gradle.kts | sed 's/.*versionName = "\(.*\)".*/\1/')
    if [ -z "$CURRENT_VERSION" ]; then
        echo -e "${RED}❌ 错误: 无法获取当前版本号${NC}"
        exit 1
    fi
    echo -e "${BLUE}📋 当前版本: $CURRENT_VERSION${NC}"
}

# 计算新版本号
calculate_new_version() {
    IFS='.' read -ra VERSION_PARTS <<< "$CURRENT_VERSION"
    MAJOR=${VERSION_PARTS[0]}
    MINOR=${VERSION_PARTS[1]}
    PATCH=${VERSION_PARTS[2]}
    
    case $VERSION_TYPE in
        major)
            MAJOR=$((MAJOR + 1))
            MINOR=0
            PATCH=0
            ;;
        minor)
            MINOR=$((MINOR + 1))
            PATCH=0
            ;;
        patch)
            PATCH=$((PATCH + 1))
            ;;
    esac
    
    NEW_VERSION="$MAJOR.$MINOR.$PATCH"
    echo -e "${GREEN}🎯 新版本: $NEW_VERSION${NC}"
}

# 运行测试
run_tests() {
    if [ "$SKIP_TESTS" = true ]; then
        echo -e "${YELLOW}⏭️  跳过测试${NC}"
        return
    fi
    
    echo -e "${BLUE}🧪 运行测试...${NC}"
    if ! ./gradlew test; then
        echo -e "${RED}❌ 测试失败，发布中止${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ 测试通过${NC}"
}

# 运行代码质量检查
run_quality_checks() {
    echo -e "${BLUE}🔍 运行代码质量检查...${NC}"
    if ! ./gradlew codeQualityCheck; then
        echo -e "${YELLOW}⚠️  代码质量检查发现问题${NC}"
        read -p "是否继续发布? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
    echo -e "${GREEN}✅ 代码质量检查完成${NC}"
}

# 更新版本号
update_version() {
    echo -e "${BLUE}📝 更新版本号...${NC}"
    
    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}[预览] 将更新 app/build.gradle.kts 中的版本号${NC}"
        return
    fi
    
    # 更新 build.gradle.kts 中的版本号
    sed -i.bak "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/" app/build.gradle.kts
    
    # 更新版本代码
    CURRENT_VERSION_CODE=$(grep 'versionCode = ' app/build.gradle.kts | sed 's/.*versionCode = \(.*\)/\1/')
    NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))
    sed -i.bak "s/versionCode = $CURRENT_VERSION_CODE/versionCode = $NEW_VERSION_CODE/" app/build.gradle.kts
    
    # 删除备份文件
    rm -f app/build.gradle.kts.bak
    
    echo -e "${GREEN}✅ 版本号已更新: $CURRENT_VERSION -> $NEW_VERSION${NC}"
    echo -e "${GREEN}✅ 版本代码已更新: $CURRENT_VERSION_CODE -> $NEW_VERSION_CODE${NC}"
}

# 构建发布版本
build_release() {
    echo -e "${BLUE}🏗️  构建发布版本...${NC}"
    
    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}[预览] 将执行: ./gradlew assembleRelease${NC}"
        return
    fi
    
    if ! ./gradlew assembleRelease; then
        echo -e "${RED}❌ 构建失败${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ 发布版本构建完成${NC}"
}

# 创建Git标签和提交
create_git_tag() {
    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}[预览] 将执行以下Git操作:${NC}"
        echo -e "${YELLOW}  git add app/build.gradle.kts${NC}"
        echo -e "${YELLOW}  git commit -m \"chore: bump version to $NEW_VERSION\"${NC}"
        echo -e "${YELLOW}  git tag -a v$NEW_VERSION -m \"Release version $NEW_VERSION\"${NC}"
        return
    fi
    
    echo -e "${BLUE}📝 创建Git提交和标签...${NC}"
    
    git add app/build.gradle.kts
    git commit -m "chore: bump version to $NEW_VERSION"
    git tag -a "v$NEW_VERSION" -m "Release version $NEW_VERSION"
    
    echo -e "${GREEN}✅ Git标签 v$NEW_VERSION 已创建${NC}"
}

# 推送到远程仓库
push_to_remote() {
    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}[预览] 将推送到远程仓库${NC}"
        return
    fi
    
    echo -e "${BLUE}🚀 推送到远程仓库...${NC}"
    
    read -p "是否推送到远程仓库? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git push origin HEAD
        git push origin "v$NEW_VERSION"
        echo -e "${GREEN}✅ 已推送到远程仓库${NC}"
    else
        echo -e "${YELLOW}⏭️  跳过推送，请手动推送:${NC}"
        echo -e "${YELLOW}  git push origin HEAD${NC}"
        echo -e "${YELLOW}  git push origin v$NEW_VERSION${NC}"
    fi
}

# 生成发布说明
generate_release_notes() {
    echo -e "${BLUE}📋 生成发布说明...${NC}"
    
    RELEASE_NOTES_FILE="release-notes-$NEW_VERSION.md"
    
    cat > "$RELEASE_NOTES_FILE" << EOF
# Release $NEW_VERSION

## 📅 发布日期
$(date '+%Y-%m-%d')

## 🎯 版本类型
$VERSION_TYPE 版本更新

## 📝 更新内容

### 🆕 新功能
- [ ] 添加新功能描述

### 🔧 改进
- [ ] 性能优化
- [ ] UI/UX 改进

### 🐛 修复
- [ ] 修复已知问题

### 🏗️ 技术改进
- [ ] 代码重构
- [ ] 依赖更新

## 📊 技术指标
- 构建时间: $(date)
- 版本代码: $NEW_VERSION_CODE
- 最小SDK: 23
- 目标SDK: 36

## 🔗 相关链接
- [GitHub Release](https://github.com/your-repo/releases/tag/v$NEW_VERSION)
- [APK下载](https://github.com/your-repo/releases/download/v$NEW_VERSION/DemoApp-$NEW_VERSION.apk)

---
*此发布说明由自动化脚本生成，请根据实际情况修改*
EOF

    echo -e "${GREEN}✅ 发布说明已生成: $RELEASE_NOTES_FILE${NC}"
    echo -e "${BLUE}💡 请编辑发布说明文件，然后在GitHub上创建Release${NC}"
}

# 主流程
main() {
    echo -e "${GREEN}🚀 开始版本发布流程...${NC}"
    echo -e "${BLUE}📋 发布类型: $VERSION_TYPE${NC}"
    
    if [ "$DRY_RUN" = true ]; then
        echo -e "${YELLOW}🔍 预览模式 - 不会实际执行操作${NC}"
    fi
    
    check_git_status
    get_current_version
    calculate_new_version
    
    echo ""
    echo -e "${BLUE}📋 发布摘要:${NC}"
    echo -e "  当前版本: $CURRENT_VERSION"
    echo -e "  新版本: $NEW_VERSION"
    echo -e "  发布类型: $VERSION_TYPE"
    echo -e "  预览模式: $DRY_RUN"
    echo -e "  跳过测试: $SKIP_TESTS"
    echo ""
    
    if [ "$DRY_RUN" = false ]; then
        read -p "确认发布? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo -e "${YELLOW}❌ 发布已取消${NC}"
            exit 0
        fi
    fi
    
    run_tests
    run_quality_checks
    update_version
    build_release
    create_git_tag
    generate_release_notes
    push_to_remote
    
    echo ""
    echo -e "${GREEN}🎉 版本 $NEW_VERSION 发布完成！${NC}"
    echo ""
    echo -e "${BLUE}📋 后续步骤:${NC}"
    echo -e "  1. 编辑发布说明: $RELEASE_NOTES_FILE"
    echo -e "  2. 在GitHub上创建Release"
    echo -e "  3. 上传APK文件到Release"
    echo -e "  4. 通知团队新版本发布"
}

# 执行主流程
main