package lj.sword.demoappnocompose.ext

import android.view.View

/**
 * 设置View可见性为VISIBLE
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * 设置View可见性为INVISIBLE
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * 设置View可见性为GONE
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * 根据条件设置View可见性
 * @param visible true=VISIBLE, false=GONE
 */
fun View.visibleOrGone(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * 根据条件设置View可见性
 * @param visible true=VISIBLE, false=INVISIBLE
 */
fun View.visibleOrInvisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

/**
 * 判断View是否可见
 */
fun View.isVisible(): Boolean = visibility == View.VISIBLE

/**
 * 防抖点击
 * @param interval 点击间隔（毫秒）
 * @param action 点击事件
 */
private var lastClickTime = 0L

fun View.setOnClickListener(
    interval: Long = 500,
    action: (View) -> Unit,
) {
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            action(it)
        }
    }
}

/**
 * 防抖点击（简化版）
 */
fun View.onSingleClick(
    interval: Long = 500,
    action: () -> Unit,
) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            action()
        }
    }
}
