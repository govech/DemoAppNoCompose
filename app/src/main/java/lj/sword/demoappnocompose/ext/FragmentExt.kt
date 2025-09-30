package lj.sword.demoappnocompose.ext

import androidx.fragment.app.Fragment

/**
 * Fragment 扩展函数
 * 
 * @author Sword
 * @since 1.0.0
 */

/**
 * 显示 Toast
 */
fun Fragment.toast(message: String) {
    requireContext().toast(message)
}

/**
 * 显示 Toast（字符串资源）
 */
fun Fragment.toast(messageResId: Int) {
    requireContext().toast(messageResId)
}

/**
 * dp 转 px
 */
fun Fragment.dp2px(dp: Float): Int {
    return requireContext().dp2px(dp)
}

/**
 * dp 转 px（Int）
 */
fun Fragment.dp2px(dp: Int): Int {
    return requireContext().dp2px(dp)
}
