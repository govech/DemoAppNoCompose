package lj.sword.demoappnocompose.utils

import android.content.Context
import androidx.annotation.StringRes
import lj.sword.demoappnocompose.app.BaseApplication

/**
 * 字符串资源工具类
 * 提供全局字符串资源访问和格式化功能
 * 
 * @author Sword
 * @since 1.0.0
 */
object StringResourceUtils {

    /**
     * 获取字符串资源
     * 
     * @param resId 字符串资源ID
     * @return 本地化后的字符串
     */
    fun getString(@StringRes resId: Int): String {
        return try {
            BaseApplication.context.getString(resId)
        } catch (e: Exception) {
            e.printStackTrace()
            "String not found"
        }
    }

    /**
     * 获取格式化的字符串资源
     * 
     * @param resId 字符串资源ID
     * @param args 格式化参数
     * @return 格式化后的字符串
     */
    fun getString(@StringRes resId: Int, vararg args: Any): String {
        return try {
            BaseApplication.context.getString(resId, *args)
        } catch (e: Exception) {
            e.printStackTrace()
            "String not found"
        }
    }

    /**
     * 获取字符串数组资源
     * 
     * @param resId 字符串数组资源ID
     * @return 字符串数组
     */
    fun getStringArray(@StringRes resId: Int): Array<String> {
        return try {
            BaseApplication.context.resources.getStringArray(resId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyArray()
        }
    }

    /**
     * 获取复数形式字符串
     * 
     * @param resId 复数资源ID
     * @param quantity 数量
     * @param args 格式化参数
     * @return 复数形式字符串
     */
    fun getQuantityString(@StringRes resId: Int, quantity: Int, vararg args: Any): String {
        return try {
            BaseApplication.context.resources.getQuantityString(resId, quantity, *args)
        } catch (e: Exception) {
            e.printStackTrace()
            "String not found"
        }
    }

    /**
     * 安全获取字符串资源（带默认值）
     * 
     * @param resId 字符串资源ID
     * @param default 默认值
     * @return 字符串资源或默认值
     */
    fun getStringOrDefault(@StringRes resId: Int, default: String): String {
        return try {
            val result = BaseApplication.context.getString(resId)
            if (result.isEmpty()) default else result
        } catch (e: Exception) {
            default
        }
    }

    /**
     * 检查字符串资源是否存在
     * 
     * @param resId 字符串资源ID
     * @return 是否存在
     */
    fun hasString(@StringRes resId: Int): Boolean {
        return try {
            BaseApplication.context.getString(resId)
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Context 扩展函数
 */
fun Context.getStringSafe(@StringRes resId: Int): String {
    return StringResourceUtils.getString(resId)
}

fun Context.getStringSafe(@StringRes resId: Int, vararg args: Any): String {
    return StringResourceUtils.getString(resId, *args)
}

/**
 * 全局字符串资源访问函数
 */
fun getString(@StringRes resId: Int): String = StringResourceUtils.getString(resId)

fun getString(@StringRes resId: Int, vararg args: Any): String = StringResourceUtils.getString(resId, *args)

fun getStringArray(@StringRes resId: Int): Array<String> = StringResourceUtils.getStringArray(resId)

fun getQuantityString(@StringRes resId: Int, quantity: Int, vararg args: Any): String = 
    StringResourceUtils.getQuantityString(resId, quantity, *args)







