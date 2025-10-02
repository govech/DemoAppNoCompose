# 🎨 控件主题跟随完整指南

## 📋 核心原则

让控件跟随主题的**黄金法则**：
- ✅ **使用主题属性** `?attr/xxx`
- ❌ **避免硬编码颜色** `@color/xxx` 或 `#RRGGBB`

## 🎯 常用主题属性速查表

### 🎨 颜色属性

#### 主色系
```xml
?attr/colorPrimary          <!-- 主色：按钮、强调色 -->
?attr/colorPrimaryVariant   <!-- 主色变体：状态栏、深色版本 -->
?attr/colorOnPrimary        <!-- 主色上的内容：主色背景上的文字/图标 -->
```

#### 次色系
```xml
?attr/colorSecondary        <!-- 次色：辅助按钮、开关 -->
?attr/colorSecondaryVariant <!-- 次色变体 -->
?attr/colorOnSecondary      <!-- 次色上的内容 -->
```

#### 背景色系
```xml
?attr/colorBackground       <!-- 页面背景色 -->
?attr/colorSurface          <!-- 卡片、对话框背景色 -->
?attr/colorOnBackground     <!-- 背景上的内容色 -->
?attr/colorOnSurface        <!-- 表面上的内容色 -->
```

#### 文本色系
```xml
?attr/textColorPrimary      <!-- 主要文本：标题、重要内容 -->
?attr/textColorSecondary    <!-- 次要文本：描述、辅助信息 -->
?attr/textColorTertiary     <!-- 第三级文本：提示、占位符 -->
?attr/textColorDisabled     <!-- 禁用文本 -->
```

#### 功能色系
```xml
?attr/colorError            <!-- 错误色：错误提示、删除按钮 -->
?attr/colorSuccess          <!-- 成功色：成功提示、确认按钮 -->
?attr/colorWarning          <!-- 警告色：警告提示 -->
?attr/colorInfo             <!-- 信息色：信息提示 -->
```

#### 边框分割线
```xml
?attr/colorDivider          <!-- 分割线颜色 -->
?attr/colorBorder           <!-- 边框颜色 -->
```

## 🛠️ 常见控件主题适配

### 1. TextView / 文本控件
```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="标题文字"
    android:textColor="?attr/textColorPrimary"     <!-- 主要文本色 -->
    android:background="?attr/colorSurface" />     <!-- 表面背景色 -->

<TextView
    android:text="描述文字"
    android:textColor="?attr/textColorSecondary" /><!-- 次要文本色 -->

<TextView
    android:text="提示文字"
    android:textColor="?attr/textColorTertiary" /> <!-- 第三级文本色 -->
```

### 2. Button / 按钮控件
```xml
<!-- 主要按钮 -->
<Button
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="主要按钮"
    android:backgroundTint="?attr/colorPrimary"    <!-- 主色背景 -->
    android:textColor="?attr/colorOnPrimary" />    <!-- 主色上的文字 -->

<!-- 次要按钮 -->
<Button
    android:text="次要按钮"
    android:backgroundTint="?attr/colorSecondary"  <!-- 次色背景 -->
    android:textColor="?attr/colorOnSecondary" />  <!-- 次色上的文字 -->

<!-- 文字按钮 -->
<Button
    style="@style/Widget.Material3.Button.TextButton"
    android:text="文字按钮"
    android:textColor="?attr/colorPrimary" />      <!-- 主色文字 -->
```

### 3. EditText / 输入框
```xml
<com.google.android.material.textfield.TextInputLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="请输入内容"
    app:boxBackgroundColor="?attr/colorSurface"    <!-- 背景色 -->
    app:hintTextColor="?attr/textColorSecondary">   <!-- 提示文字色 -->
    
    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textColor="?attr/textColorPrimary"      <!-- 输入文字色 -->
        android:textColorHint="?attr/textColorTertiary" /><!-- 占位符色 -->
        
</com.google.android.material.textfield.TextInputLayout>
```

### 4. CardView / 卡片控件
```xml
<androidx.cardview.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardBackgroundColor="?attr/colorSurface"   <!-- 卡片背景色 -->
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <TextView
            android:text="卡片标题"
            android:textColor="?attr/textColorPrimary" />
            
        <TextView
            android:text="卡片内容"
            android:textColor="?attr/textColorSecondary" />
            
    </LinearLayout>
    
</androidx.cardview.widget.CardView>
```

### 5. ImageView / 图标控件
```xml
<ImageView
    android:layout_width="24dp"
    android:layout_height="24dp"
    android:src="@drawable/ic_settings"
    android:tint="?attr/colorPrimary" />            <!-- 图标着色 -->

<ImageView
    android:src="@drawable/ic_info"
    android:tint="?attr/textColorSecondary" />      <!-- 次要图标色 -->
```

### 6. Switch / 开关控件
```xml
<Switch
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:thumbTint="?attr/colorPrimary"         <!-- 滑块颜色 -->
    android:trackTint="?attr/colorSecondary" />    <!-- 轨道颜色 -->
```

### 7. ProgressBar / 进度条
```xml
<ProgressBar
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:indeterminateTint="?attr/colorPrimary" /><!-- 进度条颜色 -->
```

### 8. RecyclerView / 列表控件
```xml
<androidx.recyclerview.widget.RecyclerView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?attr/colorBackground" />  <!-- 列表背景色 -->
```

### 9. Toolbar / 工具栏
```xml
<com.google.android.material.appbar.MaterialToolbar
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize"
    android:background="?attr/colorPrimary"        <!-- 工具栏背景 -->
    app:titleTextColor="?attr/colorOnPrimary"      <!-- 标题文字色 -->
    app:navigationIconTint="?attr/colorOnPrimary" /><!-- 导航图标色 -->
```

### 10. 分割线 / Divider
```xml
<View
    android:layout_width="match_parent"
    android:layout_height="1dp"
    android:background="?attr/colorDivider" />     <!-- 分割线颜色 -->
```

## 🎨 自定义Drawable主题适配

### 1. 创建主题化的Shape
```xml
<!-- res/drawable/bg_button_primary.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="?attr/colorPrimary" />   <!-- 使用主题属性 -->
    <corners android:radius="8dp" />
</shape>
```

### 2. 创建主题化的Selector
```xml
<!-- res/drawable/bg_button_selector.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="?attr/colorPrimaryVariant" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="?attr/colorPrimary" />
            <corners android:radius="8dp" />
        </shape>
    </item>
</selector>
```

## 💻 代码中动态设置主题颜色

### 1. 使用扩展函数（推荐）
```kotlin
// 已有的扩展函数
textView.setThemeTextColor()           // 设置主题文本色
imageView.setThemeTint()               // 设置主题图标色
view.setThemeBackground()              // 设置主题背景色
```

### 2. 使用ThemeManager获取颜色
```kotlin
// 获取主题颜色
val primaryColor = themeManager.getThemeColor(context, R.attr.colorPrimary)
val textColor = themeManager.getThemeColor(context, R.attr.textColorPrimary)

// 应用到控件
button.setBackgroundColor(primaryColor)
textView.setTextColor(textColor)
```

### 3. 使用Context扩展函数
```kotlin
// 获取主题颜色
val primaryColor = context.getPrimaryColor()
val textColor = context.getTextPrimaryColor()
val backgroundColor = context.getBackgroundColor()

// 应用颜色
view.setBackgroundColor(backgroundColor)
```

## 🚨 常见错误避免

### ❌ 错误做法
```xml
<!-- 硬编码颜色 -->
android:textColor="#000000"
android:background="#FFFFFF"
android:tint="@color/default_primary"

<!-- 固定颜色资源 -->
android:textColor="@color/black"
android:background="@color/white"
```

### ✅ 正确做法
```xml
<!-- 使用主题属性 -->
android:textColor="?attr/textColorPrimary"
android:background="?attr/colorSurface"
android:tint="?attr/colorPrimary"
```

## 🧪 测试检查清单

添加新控件后，检查以下项目：

### 布局文件检查
- [ ] 所有颜色都使用了主题属性 `?attr/xxx`
- [ ] 没有硬编码颜色 `#RRGGBB` 或 `@color/xxx`
- [ ] 文本使用了合适的文本色级别
- [ ] 背景使用了合适的背景色

### 功能测试
- [ ] 切换到默认主题，控件显示正常
- [ ] 切换到商务主题，控件颜色正确变化
- [ ] 切换到活力主题，控件颜色正确变化
- [ ] 开启暗黑模式，控件适配夜间颜色
- [ ] 文字对比度符合可读性要求

### 代码检查
- [ ] 动态设置颜色使用了主题管理器
- [ ] 没有硬编码颜色值
- [ ] 使用了扩展函数简化代码

## 📚 实际示例

### 添加一个新的信息卡片
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="?attr/colorSurface"        <!-- 表面背景色 -->
    android:orientation="vertical"
    android:padding="16dp">
    
    <!-- 标题 -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="信息标题"
        android:textColor="?attr/textColorPrimary"  <!-- 主要文本色 -->
        android:textSize="18sp"
        android:textStyle="bold" />
    
    <!-- 内容 -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="这是信息内容"
        android:textColor="?attr/textColorSecondary" /><!-- 次要文本色 -->
    
    <!-- 操作按钮 -->
    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:backgroundTint="?attr/colorPrimary"  <!-- 主色背景 -->
        android:text="查看详情"
        android:textColor="?attr/colorOnPrimary" />  <!-- 主色上文字 -->
        
</LinearLayout>
```

## 🎉 总结

遵循这个指南，你添加的任何新控件都能完美跟随主题切换：

1. **布局文件**：始终使用 `?attr/xxx` 主题属性
2. **代码设置**：使用ThemeManager或扩展函数获取主题颜色
3. **测试验证**：在所有主题下测试控件显示效果

记住核心原则：**用主题属性，避免硬编码！** 🚀