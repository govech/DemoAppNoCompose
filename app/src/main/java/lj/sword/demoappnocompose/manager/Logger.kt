package lj.sword.demoappnocompose.manager

import android.util.Log
import lj.sword.demoappnocompose.BuildConfig
import lj.sword.demoappnocompose.config.AppConfig

/**
 * 日志管理器
 * 提供统一的日志输出功能
 *
 * @author Sword
 * @since 1.0.0
 */
object Logger {
    private const val TAG = "DemoApp"

    /**
     * 是否启用日志
     */
    var isEnabled = BuildConfig.DEBUG
        private set

    /**
     * 初始化日志配置
     */
    fun init(config: AppConfig) {
        isEnabled = config.enableLog
    }

    /**
     * Debug 级别日志
     */
    @JvmStatic
    fun d(
        message: String,
        tag: String = TAG,
    ) {
        if (isEnabled) {
            Log.d(tag, message)
        }
    }

    /**
     * Info 级别日志
     */
    @JvmStatic
    fun i(
        message: String,
        tag: String = TAG,
    ) {
        if (isEnabled) {
            Log.i(tag, message)
        }
    }

    /**
     * Warn 级别日志
     */
    @JvmStatic
    fun w(
        message: String,
        tag: String = TAG,
    ) {
        if (isEnabled) {
            Log.w(tag, message)
        }
    }

    /**
     * Error 级别日志
     */
    @JvmStatic
    fun e(
        message: String,
        throwable: Throwable? = null,
        tag: String = TAG,
    ) {
        if (isEnabled) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }

    /**
     * JSON 日志（格式化输出）
     */
    @JvmStatic
    fun json(
        json: String,
        tag: String = TAG,
    ) {
        if (isEnabled) {
            try {
                val formatted = lj.sword.demoappnocompose.utils.JsonUtil.formatJson(json)
                Log.d(tag, "\n$formatted")
            } catch (e: Exception) {
                Log.d(tag, json)
            }
        }
    }

    /**
     * 网络请求日志
     */
    @JvmStatic
    fun network(
        url: String,
        method: String,
        params: String? = null,
        tag: String = TAG,
    ) {
        if (isEnabled) {
            val log =
                buildString {
                    appendLine("==================== Network ====================")
                    appendLine("URL: $url")
                    appendLine("Method: $method")
                    if (params != null) {
                        appendLine("Params: $params")
                    }
                    appendLine("================================================")
                }
            Log.d(tag, log)
        }
    }
}
