package lj.sword.demoappnocompose.utils.ext

import android.content.Context
import android.util.TypedValue
import androidx.core.content.ContextCompat
import lj.sword.demoappnocompose.R

/**
 * 颜色扩展函数
 */

/**
 * 根据主题属性获取颜色
 */
fun Context.getThemeColor(attrRes: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return ContextCompat.getColor(this, typedValue.resourceId)
}

/**
 * 获取主色
 */
fun Context.getPrimaryColor(): Int = getThemeColor(R.attr.colorPrimary)

/**
 * 获取主色变体
 */
fun Context.getPrimaryVariantColor(): Int = getThemeColor(R.attr.colorPrimaryVariant)

/**
 * 获取主色上的内容色
 */
fun Context.getOnPrimaryColor(): Int = getThemeColor(R.attr.colorOnPrimary)

/**
 * 获取次色
 */
fun Context.getSecondaryColor(): Int = getThemeColor(R.attr.colorSecondary)

/**
 * 获取次色变体
 */
fun Context.getSecondaryVariantColor(): Int = getThemeColor(R.attr.colorSecondaryVariant)

/**
 * 获取次色上的内容色
 */
fun Context.getOnSecondaryColor(): Int = getThemeColor(R.attr.colorOnSecondary)

/**
 * 获取背景色
 */
fun Context.getBackgroundColor(): Int = getThemeColor(R.attr.colorBackground)

/**
 * 获取表面色
 */
fun Context.getSurfaceColor(): Int = getThemeColor(R.attr.colorSurface)

/**
 * 获取背景上的内容色
 */
fun Context.getOnBackgroundColor(): Int = getThemeColor(R.attr.colorOnBackground)

/**
 * 获取表面上的内容色
 */
fun Context.getOnSurfaceColor(): Int = getThemeColor(R.attr.colorOnSurface)

/**
 * 获取错误色
 */
fun Context.getErrorColor(): Int = getThemeColor(R.attr.colorError)

/**
 * 获取成功色
 */
fun Context.getSuccessColor(): Int = getThemeColor(R.attr.colorSuccess)

/**
 * 获取警告色
 */
fun Context.getWarningColor(): Int = getThemeColor(R.attr.colorWarning)

/**
 * 获取信息色
 */
fun Context.getInfoColor(): Int = getThemeColor(R.attr.colorInfo)

/**
 * 获取主要文本色
 */
fun Context.getTextPrimaryColor(): Int = getThemeColor(R.attr.textColorPrimary)

/**
 * 获取次要文本色
 */
fun Context.getTextSecondaryColor(): Int = getThemeColor(R.attr.textColorSecondary)

/**
 * 获取第三级文本色
 */
fun Context.getTextTertiaryColor(): Int = getThemeColor(R.attr.textColorTertiary)

/**
 * 获取禁用文本色
 */
fun Context.getTextDisabledColor(): Int = getThemeColor(R.attr.textColorDisabled)

/**
 * 获取分割线色
 */
fun Context.getDividerColor(): Int = getThemeColor(R.attr.colorDivider)

/**
 * 获取边框色
 */
fun Context.getBorderColor(): Int = getThemeColor(R.attr.colorBorder)

/**
 * 调整颜色透明度
 */
fun Int.withAlpha(alpha: Float): Int {
    val alphaInt = (alpha * 255).toInt().coerceIn(0, 255)
    return (this and 0x00FFFFFF) or (alphaInt shl 24)
}

/**
 * 加深颜色
 */
fun Int.darken(factor: Float): Int {
    val factor = factor.coerceIn(0f, 1f)
    val alpha = (this shr 24) and 0xFF
    val red = ((this shr 16) and 0xFF) * (1 - factor)
    val green = ((this shr 8) and 0xFF) * (1 - factor)
    val blue = (this and 0xFF) * (1 - factor)
    return (alpha shl 24) or (red.toInt() shl 16) or (green.toInt() shl 8) or blue.toInt()
}

/**
 * 减淡颜色
 */
fun Int.lighten(factor: Float): Int {
    val factor = factor.coerceIn(0f, 1f)
    val alpha = (this shr 24) and 0xFF
    val red = ((this shr 16) and 0xFF) + (255 - ((this shr 16) and 0xFF)) * factor
    val green = ((this shr 8) and 0xFF) + (255 - ((this shr 8) and 0xFF)) * factor
    val blue = (this and 0xFF) + (255 - (this and 0xFF)) * factor
    return (alpha shl 24) or (red.toInt() shl 16) or (green.toInt() shl 8) or blue.toInt()
}
