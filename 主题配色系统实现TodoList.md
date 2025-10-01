# 🎨 主题配色系统实现 TodoList

## 📋 Phase 1: 基础设计与规划

- [x] 制定颜色命名规范
- [x] 定义颜色层级体系（Primary/Secondary/Background/Surface/功能色/文本色/边框色）
- [x] 使用设计工具设计至少3套主题配色方案
- [x] 为每套主题设计日间+夜间两种模式
- [x] 确定主题切换方式（重启Activity/动态刷新）
- [x] 设计Theme实体类数据结构
- [x] 设计ThemeManager架构
- [x] 确定主题存储方案（DataStore）

---

## 📋 Phase 2: 资源文件创建

### 颜色资源
- [x] 创建 `res/values/colors.xml`
- [x] 定义主题1的所有颜色值
- [x] 定义主题2的所有颜色值
- [x] 定义主题3的所有颜色值
- [x] 定义通用固定颜色
- [x] 定义功能色（成功/警告/错误/信息）

### 自定义属性
- [x] 创建 `res/values/attrs.xml`
- [x] 定义 colorPrimary 属性
- [x] 定义 colorPrimaryVariant 属性
- [x] 定义 colorOnPrimary 属性
- [x] 定义 colorSecondary 属性
- [x] 定义 colorSecondaryVariant 属性
- [x] 定义 colorOnSecondary 属性
- [x] 定义 colorBackground 属性
- [x] 定义 colorSurface 属性
- [x] 定义 colorOnBackground 属性
- [x] 定义 colorOnSurface 属性
- [x] 定义 colorError 属性
- [x] 定义 colorSuccess 属性
- [x] 定义 colorWarning 属性
- [x] 定义 colorInfo 属性
- [x] 定义 textColorPrimary 属性
- [x] 定义 textColorSecondary 属性
- [x] 定义 textColorTertiary 属性
- [x] 定义 textColorDisabled 属性
- [x] 定义 colorDivider 属性
- [x] 定义 colorBorder 属性

### 日间主题样式
- [x] 创建 `res/values/themes.xml`
- [x] 创建基础 AppTheme（继承Material主题）
- [x] 创建 DefaultTheme 并配置所有颜色属性
- [x] 配置 DefaultTheme 的状态栏颜色
- [x] 配置 DefaultTheme 的导航栏颜色
- [x] 配置 DefaultTheme 的窗口背景
- [x] 配置 DefaultTheme 的 ActionBar 样式
- [x] 创建 BusinessTheme 并配置所有颜色属性
- [x] 配置 BusinessTheme 的系统属性
- [x] 创建 VibrantTheme 并配置所有颜色属性
- [x] 配置 VibrantTheme 的系统属性

### 夜间主题样式
- [x] 创建 `res/values-night/themes.xml`
- [x] 创建 DefaultTheme.Night 并调整为深色系
- [x] 确保 DefaultTheme.Night 对比度符合可读性
- [x] 创建 BusinessTheme.Night
- [x] 确保 BusinessTheme.Night 对比度符合可读性
- [x] 创建 VibrantTheme.Night
- [x] 确保 VibrantTheme.Night 对比度符合可读性

---

## 📋 Phase 3: 数据层实现

### 数据模型
- [x] 创建 `model/Theme.kt`
- [x] 创建 AppTheme 枚举类
- [x] 为 AppTheme 添加 themeId 属性
- [x] 为 AppTheme 添加 themeName 属性
- [x] 为 AppTheme 添加 styleRes 属性
- [x] 为 AppTheme 添加 nightStyleRes 属性
- [x] 定义 DEFAULT 主题枚举值
- [x] 定义 BUSINESS 主题枚举值
- [x] 定义 VIBRANT 主题枚举值
- [x] 创建 ThemeConfig 数据类
- [x] 为 ThemeConfig 添加 currentTheme 属性
- [x] 为 ThemeConfig 添加 isDarkMode 属性
- [x] 为 ThemeConfig 添加 followSystem 属性

### 数据存储
- [x] 创建 `data/local/datastore/ThemeDataStore.kt`
- [x] 创建 DataStore 配置
- [x] 实现保存主题ID方法
- [x] 实现获取主题ID方法（返回Flow）
- [x] 实现保存暗黑模式设置方法
- [x] 实现获取暗黑模式设置方法（返回Flow）
- [x] 实现保存跟随系统设置方法
- [x] 实现获取跟随系统设置方法（返回Flow）
- [x] 添加默认值处理

---

## 📋 Phase 4: 核心管理器实现

### ThemeManager 基础
- [x] 创建 `manager/ThemeManager.kt`
- [x] 创建单例 ThemeManager 类
- [x] 注入 ThemeDataStore 依赖（Hilt）
- [x] 实现初始化方法
- [x] 实现获取当前主题配置方法（返回Flow）

### 主题切换逻辑
- [x] 实现切换主题方法
- [x] 实现切换暗黑模式方法
- [x] 实现设置跟随系统方法
- [x] 实现应用主题到Activity方法
- [x] 根据isDark选择日间/夜间theme逻辑
- [x] 调用setTheme应用主题
- [x] 调用recreate重启Activity

### 颜色获取工具
- [x] 实现根据属性获取颜色方法
- [x] 使用TypedValue从主题解析颜色值
- [x] 实现颜色透明度调整方法
- [x] 创建Context.getThemeColor扩展函数
- [x] 创建Context.getPrimaryColor扩展函数
- [x] 创建Context.getSecondaryColor扩展函数
- [x] 创建Context.getBackgroundColor扩展函数
- [x] 创建Context.getSurfaceColor扩展函数
- [x] 创建更多便捷颜色获取方法

### 系统暗黑模式监听
- [x] 实现系统暗黑模式变化监听方法
- [x] 监听Configuration变化
- [x] 当跟随系统时自动切换主题

---

## 📋 Phase 5: BaseActivity 集成

### 主题应用
- [x] 修改 `base/BaseActivity.kt`
- [x] 在onCreate之前调用applyTheme
- [x] 实现applyTheme私有方法
- [x] 从ThemeManager获取当前主题配置
- [x] 判断是否跟随系统
- [x] 如果跟随系统，检测系统暗黑模式
- [x] 调用ThemeManager应用主题
- [x] 实现判断系统是否暗黑模式方法

### 状态栏/导航栏适配
- [x] 根据主题调整状态栏图标颜色
- [x] 适配Android 6.0+状态栏
- [x] 适配Android 8.0+导航栏
- [x] 适配刘海屏/挖孔屏

### 配置变化监听
- [x] 重写onConfigurationChanged方法
- [x] 检测暗黑模式配置变化
- [x] 如果跟随系统，重新应用主题
- [x] 调用recreate重启Activity

---

## 📋 Phase 6: UI 组件适配

### XML布局适配
- [x] 遍历所有现有布局文件
- [x] 将硬编码颜色替换为主题属性
- [x] 替换所有TextView的textColor
- [x] 替换所有View的background
- [x] 替换所有ImageView的tint
- [x] 替换所有Button的backgroundTint
- [x] 替换所有Divider的background
- [x] 替换所有EditText的颜色属性
- [x] 替换所有CardView的背景色

### 通用Shape资源
- [x] 创建主色背景圆角shape
- [x] 创建次色背景圆角shape
- [x] 创建主色按钮背景shape
- [x] 创建卡片背景shape
- [x] 创建输入框背景shape
- [x] 创建分割线shape
- [x] 创建按钮按压选择器selector
- [x] 创建更多通用shape资源

### 自定义View适配
- [x] 遍历所有自定义View
- [x] 修改Paint颜色使用主题颜色
- [x] 修改Canvas绘制使用主题颜色
- [x] 添加主题变化监听（如需动态刷新）
- [x] 处理View在不同主题下的状态

### Dialog和弹窗适配
- [x] 修改LoadingDialog使用主题颜色
- [x] 修改CommonDialog使用主题颜色
- [x] 修改BottomSheet样式使用主题
- [x] 修改AlertDialog样式使用主题
- [x] 确保所有弹窗背景跟随主题

---

## 📋 Phase 7: 主题设置页面

### UI创建
- [x] 创建 `ui/settings/ThemeSettingsActivity.kt`
- [x] 创建对应布局文件
- [x] 设计主题选择区域（RadioGroup/RecyclerView）
- [x] 添加暗黑模式开关Switch
- [x] 添加跟随系统开关Switch
- [x] 添加主题预览区域（可选）
- [x] 设计主题预览卡片UI

### ViewModel创建
- [x] 创建 `ui/settings/ThemeSettingsViewModel.kt`
- [x] 注入ThemeManager
- [x] 暴露当前主题配置StateFlow
- [x] 实现切换主题方法
- [x] 实现切换暗黑模式方法
- [x] 实现切换跟随系统方法
- [x] 处理主题切换异常

### UI和ViewModel绑定
- [x] 观察themeConfig变化更新UI
- [x] 绑定主题选择点击事件
- [x] 绑定暗黑模式Switch切换事件
- [x] 绑定跟随系统Switch切换事件
- [x] 实现切换主题后重启所有Activity（可选）
- [x] 添加主题切换动画（可选）

### 主题预览功能（可选）
- [x] 创建预览卡片展示各主题效果
- [x] 实现点击预览临时应用主题
- [x] 实现确认按钮保存选择
- [x] 实现取消按钮恢复原主题

---

## 📋 Phase 8: Hilt 依赖注入配置

- [x] 创建 `di/ThemeModule.kt`
- [x] 使用@Module和@InstallIn注解
- [x] 提供ThemeDataStore单例
- [x] 提供ThemeManager单例
- [x] 配置依赖关系

---

## 📋 Phase 9: 工具类和扩展函数

### 颜色扩展函数
- [x] 创建 `utils/ext/ColorExt.kt`
- [x] 实现Context.getThemeColor扩展
- [x] 实现Context.getPrimaryColor扩展
- [x] 实现Context.getSecondaryColor扩展
- [x] 实现Context.getBackgroundColor扩展
- [x] 实现Context.getSurfaceColor扩展
- [x] 实现Context.getErrorColor扩展
- [x] 实现Context.getTextPrimaryColor扩展
- [x] 实现Context.getTextSecondaryColor扩展
- [x] 实现Int.withAlpha扩展（调整透明度）
- [x] 实现Int.darken扩展（加深颜色）
- [x] 实现Int.lighten扩展（减淡颜色）

### View扩展函数
- [x] 创建 `utils/ext/ViewExt.kt`
- [x] 实现View.setThemeBackground扩展
- [x] 实现TextView.setThemeTextColor扩展
- [x] 实现ImageView.setThemeTint扩展
- [x] 实现更多View快捷设置方法

---

## 📋 Phase 10: 测试与优化

### 功能测试
- [x] 测试主题切换是否生效
- [x] 测试暗黑模式切换
- [x] 测试跟随系统设置
- [x] 测试应用重启后主题保持
- [x] 测试API 23兼容性
- [x] 测试API 26兼容性
- [x] 测试API 29兼容性
- [x] 测试API 31兼容性
- [x] 测试API 34兼容性
- [x] 测试横竖屏切换主题保持
- [x] 测试多Activity场景主题同步
- [x] 测试应用退到后台再返回主题保持

### UI适配检查
- [x] 检查首页在各主题下显示
- [x] 检查列表页在各主题下显示
- [x] 检查详情页在各主题下显示
- [x] 检查设置页在各主题下显示
- [x] 检查所有Dialog在各主题下显示
- [x] 检查文字对比度符合可读性标准
- [x] 检查暗黑模式下不刺眼
- [x] 检查所有图标Tint正确
- [x] 检查所有按钮状态正常

### 性能优化
- [x] 优化主题切换速度
- [x] 减少不必要的Activity重启
- [x] 优化颜色获取添加缓存
- [x] 使用LeakCanary检查内存泄漏
- [x] 优化DataStore读写性能
- [x] 优化主题应用流程

### 代码审查
- [x] 检查代码规范
- [x] 添加必要的KDoc注释
- [x] 移除所有硬编码颜色
- [x] 统一命名规范
- [x] 清理无用代码
- [x] 添加TODO标记待完善部分

---

## 📋 Phase 11: 文档编写

### 技术文档
- [ ] 编写主题系统架构文档
- [ ] 编写颜色规范文档
- [ ] 绘制主题切换流程图
- [ ] 编写主题存储方案说明
- [ ] 编写性能优化说明

### 使用文档
- [ ] 编写如何添加新主题指南
- [ ] 编写如何在代码中使用主题颜色指南
- [ ] 编写如何适配新页面指南
- [ ] 编写主题切换最佳实践
- [ ] 编写常见问题FAQ
- [ ] 编写注意事项清单

### 示例代码
- [ ] 提供完整示例Activity
- [ ] 提供在XML中使用主题颜色示例
- [ ] 提供在代码中使用主题颜色示例
- [ ] 提供自定义View适配示例
- [ ] 提供常见场景代码片段

---

## 📊 优先级标注

### P0 - 核心必须（Milestone 1）
- Phase 1: 基础设计与规划
- Phase 2: 资源文件创建
- Phase 3: 数据层实现
- Phase 4: 核心管理器实现
- Phase 8: Hilt 依赖注入配置

### P1 - 重要功能（Milestone 2-3）
- Phase 5: BaseActivity 集成
- Phase 6: UI 组件适配
- Phase 7: 主题设置页面
- Phase 9: 工具类和扩展函数
- Phase 10: 测试与优化

### P2 - 优化完善（Milestone 4）
- Phase 11: 文档编写

  ------

  

## 📝 注意事项

1. **建议分阶段实施**：先实现核心功能（P0），再逐步完善（P1、P2）
2. **先做少数页面试点**：完整流程跑通后再全量适配
3. **做好版本控制**：每个Phase完成后提交一次
4. **保持灵活性**：根据实际情况调整TodoList
5. **代码审查**：关键节点进行代码Review
6. **持续测试**：每完成一个Phase进行功能测试

---

