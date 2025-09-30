package lj.sword.demoappnocompose.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * 屏幕工具类
 * 提供屏幕尺寸、密度等信息获取
 * 
 * @author Sword
 * @since 1.0.0
 */
object ScreenUtil {

    /**
     * 获取屏幕宽度（像素）
     */
    @JvmStatic
    fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高度（像素）
     */
    @JvmStatic
    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    /**
     * 获取屏幕密度
     */
    @JvmStatic
    fun getScreenDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }

    /**
     * 获取屏幕密度 DPI
     */
    @JvmStatic
    fun getScreenDensityDpi(context: Context): Int {
        return context.resources.displayMetrics.densityDpi
    }

    /**
     * dp 转 px
     */
    @JvmStatic
    fun dp2px(context: Context, dp: Float): Int {
        val density = getScreenDensity(context)
        return (dp * density + 0.5f).toInt()
    }

    /**
     * px 转 dp
     */
    @JvmStatic
    fun px2dp(context: Context, px: Float): Int {
        val density = getScreenDensity(context)
        return (px / density + 0.5f).toInt()
    }

    /**
     * sp 转 px
     */
    @JvmStatic
    fun sp2px(context: Context, sp: Float): Int {
        val scaledDensity = context.resources.displayMetrics.scaledDensity
        return (sp * scaledDensity + 0.5f).toInt()
    }

    /**
     * px 转 sp
     */
    @JvmStatic
    fun px2sp(context: Context, px: Float): Int {
        val scaledDensity = context.resources.displayMetrics.scaledDensity
        return (px / scaledDensity + 0.5f).toInt()
    }
}
