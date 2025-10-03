package lj.sword.demoappnocompose.ext

import android.app.Activity

/**
 * Activity 扩展函数
 *
 * @author Sword
 * @since 1.0.0
 */

/**
 * 关闭当前 Activity
 */
@Suppress("ktlint:standard:no-consecutive-comments")
fun Activity.finishWithResult(resultCode: Int = Activity.RESULT_OK) {
    setResult(resultCode)
    finish()
}
