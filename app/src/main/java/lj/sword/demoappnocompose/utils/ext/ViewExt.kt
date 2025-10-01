package lj.sword.demoappnocompose.utils.ext

import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import lj.sword.demoappnocompose.R
import lj.sword.demoappnocompose.utils.ToastUtils

/**
 * View扩展函数
 */

/**
 * 设置View的主题背景色
 */
fun View.setThemeBackground() {
    setBackgroundColor(context.getPrimaryColor())
}

/**
 * 设置TextView的主题文本色
 */
fun TextView.setThemeTextColor() {
    setTextColor(context.getTextPrimaryColor())
}

/**
 * 设置TextView的次要主题文本色
 */
fun TextView.setThemeSecondaryTextColor() {
    setTextColor(context.getTextSecondaryColor())
}

/**
 * 设置TextView的第三级主题文本色
 */
fun TextView.setThemeTertiaryTextColor() {
    setTextColor(context.getTextTertiaryColor())
}

/**
 * 设置TextView的禁用主题文本色
 */
fun TextView.setThemeDisabledTextColor() {
    setTextColor(context.getTextDisabledColor())
}

/**
 * 设置ImageView的主题Tint
 */
fun ImageView.setThemeTint() {
    setColorFilter(context.getPrimaryColor(), PorterDuff.Mode.SRC_IN)
}

/**
 * 设置ImageView的次要主题Tint
 */
fun ImageView.setThemeSecondaryTint() {
    setColorFilter(context.getSecondaryColor(), PorterDuff.Mode.SRC_IN)
}

/**
 * 设置ImageView的错误主题Tint
 */
fun ImageView.setThemeErrorTint() {
    setColorFilter(context.getErrorColor(), PorterDuff.Mode.SRC_IN)
}

/**
 * 设置ImageView的成功主题Tint
 */
fun ImageView.setThemeSuccessTint() {
    setColorFilter(context.getSuccessColor(), PorterDuff.Mode.SRC_IN)
}

/**
 * 设置ImageView的警告主题Tint
 */
fun ImageView.setThemeWarningTint() {
    setColorFilter(context.getWarningColor(), PorterDuff.Mode.SRC_IN)
}

/**
 * 设置ImageView的信息主题Tint
 */
fun ImageView.setThemeInfoTint() {
    setColorFilter(context.getInfoColor(), PorterDuff.Mode.SRC_IN)
}

/**
 * 显示短Toast
 */
fun View.toast(message: String) {
    ToastUtils.showShort(context, message)
}

/**
 * 显示长Toast
 */
fun View.toastLong(message: String) {
    ToastUtils.showLong(context, message)
}

/**
 * 显示短Toast（资源ID）
 */
fun View.toast(resId: Int) {
    ToastUtils.showShort(context, resId)
}

/**
 * 显示长Toast（资源ID）
 */
fun View.toastLong(resId: Int) {
    ToastUtils.showLong(context, resId)
}
