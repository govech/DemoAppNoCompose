# Android MVVM 开发框架混淆配置

# ============================================
# 基础配置
# ============================================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 保留行号信息，方便调试
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 保留注解
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ============================================
# Android 基础组件
# ============================================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View

# ============================================
# 项目基类
# ============================================
-keep class lj.sword.demoappnocompose.base.** { *; }
-keep class lj.sword.demoappnocompose.app.** { *; }

# ============================================
# 数据模型（实体类）
# ============================================
-keep class lj.sword.demoappnocompose.data.model.** { *; }

# ============================================
# Gson
# ============================================
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ============================================
# Retrofit
# ============================================
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ============================================
# OkHttp
# ============================================
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ============================================
# Room
# ============================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ============================================
# Hilt
# ============================================
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel
-keepclassmembers class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper {
    *;
}

# ============================================
# Kotlin
# ============================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ============================================
# Coil
# ============================================
-keep class coil.** { *; }
-dontwarn coil.**

# ============================================
# 其他
# ============================================
# Native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding {
    *;
}

# Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}