#!/bin/bash

# 代码质量检查脚本
# 运行所有代码质量检查工具

set -e

echo "🔍 开始代码质量检查..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查是否在项目根目录
if [ ! -f "gradlew" ]; then
    echo -e "${RED}❌ 错误: 请在项目根目录运行此脚本${NC}"
    exit 1
fi

# 清理之前的报告
echo -e "${BLUE}🧹 清理之前的报告...${NC}"
rm -rf app/build/reports/

# 1. 运行 ktlint 检查
echo -e "${BLUE}📝 运行 ktlint 代码格式检查...${NC}"
if ./gradlew ktlintCheck; then
    echo -e "${GREEN}✅ ktlint 检查通过${NC}"
else
    echo -e "${YELLOW}⚠️  ktlint 检查发现问题，尝试自动修复...${NC}"
    ./gradlew ktlintFormat
    echo -e "${GREEN}✅ ktlint 自动修复完成${NC}"
fi

# 2. 运行 detekt 静态分析
echo -e "${BLUE}🔍 运行 detekt 静态代码分析...${NC}"
if ./gradlew detekt; then
    echo -e "${GREEN}✅ detekt 分析通过${NC}"
else
    echo -e "${YELLOW}⚠️  detekt 发现代码质量问题，请查看报告${NC}"
fi

# 3. 运行 Android Lint
echo -e "${BLUE}🔍 运行 Android Lint 检查...${NC}"
if ./gradlew lint; then
    echo -e "${GREEN}✅ Android Lint 检查通过${NC}"
else
    echo -e "${YELLOW}⚠️  Android Lint 发现问题，请查看报告${NC}"
fi

# 4. 运行单元测试
echo -e "${BLUE}🧪 运行单元测试...${NC}"
if ./gradlew test; then
    echo -e "${GREEN}✅ 单元测试通过${NC}"
else
    echo -e "${RED}❌ 单元测试失败${NC}"
    exit 1
fi

# 5. 生成报告摘要
echo -e "${BLUE}📊 生成报告摘要...${NC}"

REPORTS_DIR="app/build/reports"
SUMMARY_FILE="$REPORTS_DIR/quality-summary.txt"

mkdir -p "$REPORTS_DIR"
echo "代码质量检查报告摘要" > "$SUMMARY_FILE"
echo "生成时间: $(date)" >> "$SUMMARY_FILE"
echo "==============================" >> "$SUMMARY_FILE"

# ktlint 报告
if [ -f "$REPORTS_DIR/ktlint/ktlintMainSourceSetCheck.txt" ]; then
    echo "" >> "$SUMMARY_FILE"
    echo "📝 ktlint 检查结果:" >> "$SUMMARY_FILE"
    if [ -s "$REPORTS_DIR/ktlint/ktlintMainSourceSetCheck.txt" ]; then
        echo "发现格式问题，详见 ktlint 报告" >> "$SUMMARY_FILE"
    else
        echo "✅ 代码格式符合规范" >> "$SUMMARY_FILE"
    fi
fi

# detekt 报告
if [ -f "$REPORTS_DIR/detekt/detekt.txt" ]; then
    echo "" >> "$SUMMARY_FILE"
    echo "🔍 detekt 分析结果:" >> "$SUMMARY_FILE"
    DETEKT_ISSUES=$(grep -c "Issue" "$REPORTS_DIR/detekt/detekt.txt" 2>/dev/null || echo "0")
    echo "发现 $DETEKT_ISSUES 个代码质量问题" >> "$SUMMARY_FILE"
fi

# lint 报告
if [ -f "$REPORTS_DIR/lint-results.xml" ]; then
    echo "" >> "$SUMMARY_FILE"
    echo "🔍 Android Lint 结果:" >> "$SUMMARY_FILE"
    LINT_ERRORS=$(grep -c 'severity="Error"' "$REPORTS_DIR/lint-results.xml" 2>/dev/null || echo "0")
    LINT_WARNINGS=$(grep -c 'severity="Warning"' "$REPORTS_DIR/lint-results.xml" 2>/dev/null || echo "0")
    echo "错误: $LINT_ERRORS 个，警告: $LINT_WARNINGS 个" >> "$SUMMARY_FILE"
fi

# 测试报告
if [ -d "$REPORTS_DIR/tests" ]; then
    echo "" >> "$SUMMARY_FILE"
    echo "🧪 单元测试结果:" >> "$SUMMARY_FILE"
    echo "详见测试报告目录" >> "$SUMMARY_FILE"
fi

echo "" >> "$SUMMARY_FILE"
echo "==============================" >> "$SUMMARY_FILE"
echo "详细报告位置:" >> "$SUMMARY_FILE"
echo "- ktlint: $REPORTS_DIR/ktlint/" >> "$SUMMARY_FILE"
echo "- detekt: $REPORTS_DIR/detekt/" >> "$SUMMARY_FILE"
echo "- lint: $REPORTS_DIR/lint-results.html" >> "$SUMMARY_FILE"
echo "- 测试: $REPORTS_DIR/tests/" >> "$SUMMARY_FILE"

# 显示摘要
echo -e "${GREEN}📊 代码质量检查完成！${NC}"
echo -e "${BLUE}📋 报告摘要:${NC}"
cat "$SUMMARY_FILE"

echo ""
echo -e "${BLUE}💡 提示:${NC}"
echo -e "  - 查看详细报告: open $REPORTS_DIR/"
echo -e "  - 自动修复格式: ./gradlew ktlintFormat"
echo -e "  - 运行所有检查: ./gradlew codeQualityCheck"

exit 0