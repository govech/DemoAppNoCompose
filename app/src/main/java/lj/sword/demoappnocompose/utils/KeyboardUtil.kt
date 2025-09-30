package lj.sword.demoappnocompose.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * 键盘工具类
 * 提供软键盘显示/隐藏功能
 * 
 * @author Sword
 * @since 1.0.0
 */
object KeyboardUtil {

    /**
     * 显示软键盘
     */
    @JvmStatic
    fun showKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * 隐藏软键盘
     */
    @JvmStatic
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 隐藏软键盘（Activity）
     */
    @JvmStatic
    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus ?: return
        hideKeyboard(view)
    }

    /**
     * 切换软键盘显示状态
     */
    @JvmStatic
    fun toggleKeyboard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.toggleSoftInput(0, 0)
    }

    /**
     * 判断软键盘是否显示
     */
    @JvmStatic
    fun isKeyboardShowing(view: View): Boolean {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        return imm?.isActive(view) == true
    }
}
