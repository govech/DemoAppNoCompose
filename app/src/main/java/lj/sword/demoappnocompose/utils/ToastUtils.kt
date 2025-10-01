package lj.sword.demoappnocompose.utils

import android.content.Context
import android.widget.Toast

/**
 * Toast工具类
 * 
 * @author Sword
 * @since 1.0.0
 */
object ToastUtils {
    
    /**
     * 显示短Toast
     */
    fun showShort(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 显示长Toast
     */
    fun showLong(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * 显示短Toast（资源ID）
     */
    fun showShort(context: Context, resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 显示长Toast（资源ID）
     */
    fun showLong(context: Context, resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show()
    }
}







