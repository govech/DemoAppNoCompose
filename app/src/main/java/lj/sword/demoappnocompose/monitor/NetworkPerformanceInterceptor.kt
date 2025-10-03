package lj.sword.demoappnocompose.monitor

import lj.sword.demoappnocompose.config.AppConfig
import lj.sword.demoappnocompose.manager.Logger
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络性能监控拦截器
 * 监控网络请求性能
 *
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class NetworkPerformanceInterceptor
    @Inject
    constructor(
        private val appConfig: AppConfig,
    ) : Interceptor {
        companion object {
            private const val TAG = "NetworkPerformance"
            private const val SLOW_REQUEST_THRESHOLD = 3000L // 3秒
            private const val VERY_SLOW_REQUEST_THRESHOLD = 10000L // 10秒
        }

        // 网络性能监听器
        interface NetworkPerformanceListener {
            fun onRequestCompleted(performanceInfo: NetworkPerformanceInfo)

            fun onSlowRequest(performanceInfo: NetworkPerformanceInfo)

            fun onRequestFailed(
                performanceInfo: NetworkPerformanceInfo,
                error: Throwable,
            )
        }

        // 网络性能信息
        data class NetworkPerformanceInfo(
            val url: String,
            val method: String,
            val requestSize: Long,
            val responseSize: Long,
            val duration: Long,
            val responseCode: Int,
            val isSuccessful: Boolean,
            val errorMessage: String? = null,
            val timestamp: Long = System.currentTimeMillis(),
        ) {
            fun isSlowRequest(): Boolean {
                return duration > SLOW_REQUEST_THRESHOLD
            }

            fun isVerySlowRequest(): Boolean {
                return duration > VERY_SLOW_REQUEST_THRESHOLD
            }

            fun getSpeedKBps(): Double {
                return if (duration > 0) {
                    (responseSize / 1024.0) / (duration / 1000.0)
                } else {
                    0.0
                }
            }
        }

        private val listeners = mutableListOf<NetworkPerformanceListener>()

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            if (!appConfig.enablePerformanceMonitor) {
                return chain.proceed(chain.request())
            }

            val request = chain.request()
            val startTime = System.currentTimeMillis()

            var response: Response? = null
            var error: Throwable? = null

            try {
                response = chain.proceed(request)
                return response
            } catch (e: Exception) {
                error = e
                throw e
            } finally {
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime

                // 创建性能信息
                val performanceInfo =
                    createPerformanceInfo(
                        request,
                        response,
                        duration,
                        error,
                    )

                // 记录性能信息
                logPerformanceInfo(performanceInfo)

                // 通知监听器
                notifyListeners(performanceInfo, error)
            }
        }

        /**
         * 添加性能监听器
         */
        fun addPerformanceListener(listener: NetworkPerformanceListener) {
            listeners.add(listener)
        }

        /**
         * 移除性能监听器
         */
        fun removePerformanceListener(listener: NetworkPerformanceListener) {
            listeners.remove(listener)
        }

        /**
         * 创建性能信息
         */
        private fun createPerformanceInfo(
            request: Request,
            response: Response?,
            duration: Long,
            error: Throwable?,
        ): NetworkPerformanceInfo {
            val requestSize = request.body?.contentLength() ?: 0L
            val responseSize = response?.body?.contentLength() ?: 0L
            val responseCode = response?.code ?: 0
            val isSuccessful = response?.isSuccessful ?: false
            val errorMessage = error?.message

            return NetworkPerformanceInfo(
                url = request.url.toString(),
                method = request.method,
                requestSize = requestSize,
                responseSize = responseSize,
                duration = duration,
                responseCode = responseCode,
                isSuccessful = isSuccessful,
                errorMessage = errorMessage,
            )
        }

        /**
         * 记录性能信息
         */
        private fun logPerformanceInfo(info: NetworkPerformanceInfo) {
            val logMessage =
                buildString {
                    append("${info.method} ${info.url}")
                    append(" | ${info.duration}ms")
                    append(" | ${info.responseCode}")
                    if (info.responseSize > 0) {
                        append(" | ${info.responseSize / 1024}KB")
                        append(" | ${String.format("%.1f", info.getSpeedKBps())}KB/s")
                    }
                }

            when {
                !info.isSuccessful -> {
                    Logger.e("网络请求失败: $logMessage | ${info.errorMessage}", null, TAG)
                }
                info.isVerySlowRequest() -> {
                    Logger.w("网络请求非常慢: $logMessage", TAG)
                }
                info.isSlowRequest() -> {
                    Logger.w("网络请求较慢: $logMessage", TAG)
                }
                else -> {
                    Logger.d("网络请求: $logMessage", TAG)
                }
            }
        }

        /**
         * 通知监听器
         */
        private fun notifyListeners(
            info: NetworkPerformanceInfo,
            error: Throwable?,
        ) {
            listeners.forEach { listener ->
                try {
                    when {
                        error != null -> {
                            listener.onRequestFailed(info, error)
                        }
                        info.isSlowRequest() -> {
                            listener.onSlowRequest(info)
                            listener.onRequestCompleted(info)
                        }
                        else -> {
                            listener.onRequestCompleted(info)
                        }
                    }
                } catch (e: Exception) {
                    Logger.e("网络性能监听器回调失败", e, TAG)
                }
            }
        }

        /**
         * 获取网络性能统计
         */
        object NetworkPerformanceStats {
            private val requestHistory = mutableListOf<NetworkPerformanceInfo>()
            private const val MAX_HISTORY_SIZE = 100

            fun addRequest(info: NetworkPerformanceInfo) {
                synchronized(requestHistory) {
                    requestHistory.add(info)
                    if (requestHistory.size > MAX_HISTORY_SIZE) {
                        requestHistory.removeAt(0)
                    }
                }
            }

            fun getAverageResponseTime(): Long {
                synchronized(requestHistory) {
                    return if (requestHistory.isNotEmpty()) {
                        requestHistory.map { it.duration }.average().toLong()
                    } else {
                        0L
                    }
                }
            }

            fun getSuccessRate(): Float {
                synchronized(requestHistory) {
                    if (requestHistory.isEmpty()) return 0f
                    val successCount = requestHistory.count { it.isSuccessful }
                    return (successCount.toFloat() / requestHistory.size) * 100f
                }
            }

            fun getSlowRequestCount(): Int {
                synchronized(requestHistory) {
                    return requestHistory.count { it.isSlowRequest() }
                }
            }

            fun getTotalRequestCount(): Int {
                synchronized(requestHistory) {
                    return requestHistory.size
                }
            }

            fun getPerformanceReport(): String {
                synchronized(requestHistory) {
                    return buildString {
                        appendLine("=== 网络性能报告 ===")
                        appendLine("总请求数: ${getTotalRequestCount()}")
                        appendLine("成功率: ${String.format("%.1f", getSuccessRate())}%")
                        appendLine("平均响应时间: ${getAverageResponseTime()}ms")
                        appendLine("慢请求数: ${getSlowRequestCount()}")
                        if (requestHistory.isNotEmpty()) {
                            val fastestRequest = requestHistory.minByOrNull { it.duration }
                            val slowestRequest = requestHistory.maxByOrNull { it.duration }
                            appendLine("最快请求: ${fastestRequest?.duration}ms")
                            appendLine("最慢请求: ${slowestRequest?.duration}ms")
                        }
                    }
                }
            }

            fun clearHistory() {
                synchronized(requestHistory) {
                    requestHistory.clear()
                }
            }
        }
    }
