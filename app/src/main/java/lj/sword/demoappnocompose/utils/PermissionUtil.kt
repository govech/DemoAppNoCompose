package lj.sword.demoappnocompose.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 权限工具类
 * 提供权限检查和请求功能
 *
 * @author Sword
 * @since 1.0.0
 */
object PermissionUtil {
    /**
     * 检查单个权限是否已授予
     */
    @JvmStatic
    fun hasPermission(
        context: Context,
        permission: String,
    ): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission,
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查多个权限是否都已授予
     */
    @JvmStatic
    fun hasPermissions(
        context: Context,
        vararg permissions: String,
    ): Boolean {
        return permissions.all { hasPermission(context, it) }
    }

    /**
     * 请求单个权限
     */
    @JvmStatic
    fun requestPermission(
        activity: Activity,
        permission: String,
        requestCode: Int,
    ) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }

    /**
     * 请求多个权限
     */
    @JvmStatic
    fun requestPermissions(
        activity: Activity,
        permissions: Array<String>,
        requestCode: Int,
    ) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    /**
     * 检查权限请求结果
     */
    @JvmStatic
    fun checkPermissionResult(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
    }

    /**
     * 判断是否应该显示权限说明
     */
    @JvmStatic
    fun shouldShowRequestPermissionRationale(
        activity: Activity,
        permission: String,
    ): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * 过滤未授予的权限
     */
    @JvmStatic
    fun filterDeniedPermissions(
        context: Context,
        vararg permissions: String,
    ): List<String> {
        return permissions.filter { !hasPermission(context, it) }
    }
}
