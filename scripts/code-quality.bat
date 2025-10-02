@echo off
REM 代码质量检查脚本 (Windows版本)
REM 运行所有代码质量检查工具

setlocal enabledelayedexpansion

echo 🔍 开始代码质量检查...

REM 检查是否在项目根目录
if not exist "gradlew.bat" (
    echo ❌ 错误: 请在项目根目录运行此脚本
    exit /b 1
)

REM 清理之前的报告
echo 🧹 清理之前的报告...
if exist "app\build\reports\" rmdir /s /q "app\build\reports\"

REM 1. 运行 ktlint 检查
echo 📝 运行 ktlint 代码格式检查...
call gradlew.bat ktlintCheck
if !errorlevel! neq 0 (
    echo ⚠️  ktlint 检查发现问题，尝试自动修复...
    call gradlew.bat ktlintFormat
    echo ✅ ktlint 自动修复完成
) else (
    echo ✅ ktlint 检查通过
)

REM 2. 运行 detekt 静态分析
echo 🔍 运行 detekt 静态代码分析...
call gradlew.bat detekt
if !errorlevel! neq 0 (
    echo ⚠️  detekt 发现代码质量问题，请查看报告
) else (
    echo ✅ detekt 分析通过
)

REM 3. 运行 Android Lint
echo 🔍 运行 Android Lint 检查...
call gradlew.bat lint
if !errorlevel! neq 0 (
    echo ⚠️  Android Lint 发现问题，请查看报告
) else (
    echo ✅ Android Lint 检查通过
)

REM 4. 运行单元测试
echo 🧪 运行单元测试...
call gradlew.bat test
if !errorlevel! neq 0 (
    echo ❌ 单元测试失败
    exit /b 1
) else (
    echo ✅ 单元测试通过
)

REM 5. 生成报告摘要
echo 📊 生成报告摘要...
set REPORTS_DIR=app\build\reports
set SUMMARY_FILE=%REPORTS_DIR%\quality-summary.txt

if not exist "%REPORTS_DIR%" mkdir "%REPORTS_DIR%"

echo 代码质量检查报告摘要 > "%SUMMARY_FILE%"
echo 生成时间: %date% %time% >> "%SUMMARY_FILE%"
echo ============================== >> "%SUMMARY_FILE%"

REM 显示摘要
echo ✅ 代码质量检查完成！
echo 📋 报告摘要:
type "%SUMMARY_FILE%"

echo.
echo 💡 提示:
echo   - 查看详细报告: start %REPORTS_DIR%
echo   - 自动修复格式: gradlew.bat ktlintFormat  
echo   - 运行所有检查: gradlew.bat codeQualityCheck

exit /b 0