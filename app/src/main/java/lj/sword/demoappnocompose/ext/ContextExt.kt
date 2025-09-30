package lj.sword.demoappnocompose.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import lj.sword.demoappnocompose.utils.ScreenUtil

/**
 * Context 扩展函数
 * 
 * @author Sword
 * @since 1.0.0
 */

/**
 * 显示 Toast
 */
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * 显示 Toast（字符串资源）
 */
fun Context.toast(messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageResId, duration).show()
}

/**
 * 启动 Activity
 */
inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.block()
    startActivity(intent)
}

/**
 * 启动 Activity 并关闭当前页面
 */
inline fun <reified T : Activity> Activity.startActivityAndFinish(block: Intent.() -> Unit = {}) {
    startActivity<T>(block)
    finish()
}

/**
 * dp 转 px
 */
fun Context.dp2px(dp: Float): Int {
    return ScreenUtil.dp2px(this, dp)
}

/**
 * dp 转 px（Int）
 */
fun Context.dp2px(dp: Int): Int {
    return ScreenUtil.dp2px(this, dp.toFloat())
}

/**
 * px 转 dp
 */
fun Context.px2dp(px: Float): Int {
    return ScreenUtil.px2dp(this, px)
}

/**
 * sp 转 px
 */
fun Context.sp2px(sp: Float): Int {
    return ScreenUtil.sp2px(this, sp)
}

/**
 * 获取颜色
 */
fun Context.getColorCompat(colorRes: Int): Int {
    return androidx.core.content.ContextCompat.getColor(this, colorRes)
}

/**
 * 获取 Drawable
 */
fun Context.getDrawableCompat(drawableRes: Int) =
    androidx.core.content.ContextCompat.getDrawable(this, drawableRes)
