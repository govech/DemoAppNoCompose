package lj.sword.demoappnocompose.monitor

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import lj.sword.demoappnocompose.config.AppConfig
import lj.sword.demoappnocompose.manager.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 启动时间追踪器
 * 监控应用启动性能
 *
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class StartupTimeTracker
    @Inject
    constructor(
        private val appConfig: AppConfig,
    ) : Application.ActivityLifecycleCallbacks {
        companion object {
            private const val TAG = "StartupTimeTracker"
        }

        // 启动时间记录
        private var applicationStartTime: Long = 0
        private var firstActivityCreateTime: Long = 0
        private var firstActivityResumeTime: Long = 0
        private var isFirstActivityCreated = false
        private var isFirstActivityResumed = false
        private var isStartupCompleted = false

        // 启动阶段
        enum class StartupPhase {
            APPLICATION_CREATE,
            FIRST_ACTIVITY_CREATE,
            FIRST_ACTIVITY_RESUME,
            STARTUP_COMPLETED,
        }

        // 启动监听器
        interface StartupListener {
            fun onStartupPhaseCompleted(
                phase: StartupPhase,
                duration: Long,
            )

            fun onStartupCompleted(totalDuration: Long)
        }

        private val listeners = mutableListOf<StartupListener>()

        /**
         * 开始追踪启动时间
         */
        fun startTracking() {
            if (!appConfig.enablePerformanceMonitor) {
                Logger.d("性能监控已禁用，跳过启动时间追踪", TAG)
                return
            }

            applicationStartTime = SystemClock.elapsedRealtime()
            Logger.d("开始追踪应用启动时间: $applicationStartTime", TAG)
        }

        /**
         * 记录Application创建完成
         */
        fun onApplicationCreated() {
            if (!appConfig.enablePerformanceMonitor) return

            val currentTime = SystemClock.elapsedRealtime()
            val duration = currentTime - applicationStartTime

            Logger.d("Application创建完成，耗时: ${duration}ms", TAG)
            notifyPhaseCompleted(StartupPhase.APPLICATION_CREATE, duration)
        }

        /**
         * 添加启动监听器
         */
        fun addStartupListener(listener: StartupListener) {
            listeners.add(listener)
        }

        /**
         * 移除启动监听器
         */
        fun removeStartupListener(listener: StartupListener) {
            listeners.remove(listener)
        }

        /**
         * 手动标记启动完成
         * 用于复杂启动流程的场景
         */
        fun markStartupCompleted() {
            if (!appConfig.enablePerformanceMonitor || isStartupCompleted) return

            val currentTime = SystemClock.elapsedRealtime()
            val totalDuration = currentTime - applicationStartTime

            isStartupCompleted = true
            Logger.i("应用启动完成，总耗时: ${totalDuration}ms", TAG)

            // 检查是否超时
            if (totalDuration > appConfig.startupTimeout) {
                Logger.w("应用启动超时！耗时: ${totalDuration}ms，超时阈值: ${appConfig.startupTimeout}ms", TAG)
            }

            notifyStartupCompleted(totalDuration)
        }

        // Activity生命周期回调
        override fun onActivityCreated(
            activity: Activity,
            savedInstanceState: Bundle?,
        ) {
            if (!appConfig.enablePerformanceMonitor || isFirstActivityCreated) return

            firstActivityCreateTime = SystemClock.elapsedRealtime()
            val duration = firstActivityCreateTime - applicationStartTime

            isFirstActivityCreated = true
            Logger.d("首个Activity创建完成: ${activity.javaClass.simpleName}，耗时: ${duration}ms", TAG)
            notifyPhaseCompleted(StartupPhase.FIRST_ACTIVITY_CREATE, duration)
        }

        override fun onActivityStarted(activity: Activity) {
            // 不需要处理
        }

        override fun onActivityResumed(activity: Activity) {
            if (!appConfig.enablePerformanceMonitor || isFirstActivityResumed) return

            firstActivityResumeTime = SystemClock.elapsedRealtime()
            val duration = firstActivityResumeTime - applicationStartTime

            isFirstActivityResumed = true
            Logger.d("首个Activity Resume完成: ${activity.javaClass.simpleName}，耗时: ${duration}ms", TAG)
            notifyPhaseCompleted(StartupPhase.FIRST_ACTIVITY_RESUME, duration)

            // 自动标记启动完成（如果没有手动标记）
            if (!isStartupCompleted) {
                markStartupCompleted()
            }
        }

        override fun onActivityPaused(activity: Activity) {
            // 不需要处理
        }

        override fun onActivityStopped(activity: Activity) {
            // 不需要处理
        }

        override fun onActivitySaveInstanceState(
            activity: Activity,
            outState: Bundle,
        ) {
            // 不需要处理
        }

        override fun onActivityDestroyed(activity: Activity) {
            // 不需要处理
        }

        /**
         * 通知阶段完成
         */
        private fun notifyPhaseCompleted(
            phase: StartupPhase,
            duration: Long,
        ) {
            listeners.forEach { listener ->
                try {
                    listener.onStartupPhaseCompleted(phase, duration)
                } catch (e: Exception) {
                    Logger.e("启动监听器回调失败", e, TAG)
                }
            }
        }

        /**
         * 通知启动完成
         */
        private fun notifyStartupCompleted(totalDuration: Long) {
            listeners.forEach { listener ->
                try {
                    listener.onStartupCompleted(totalDuration)
                } catch (e: Exception) {
                    Logger.e("启动完成监听器回调失败", e, TAG)
                }
            }
        }

        /**
         * 获取启动统计信息
         */
        fun getStartupStats(): StartupStats {
            return StartupStats(
                applicationStartTime = applicationStartTime,
                firstActivityCreateTime = firstActivityCreateTime,
                firstActivityResumeTime = firstActivityResumeTime,
                isStartupCompleted = isStartupCompleted,
                totalDuration =
                    if (isStartupCompleted) {
                        SystemClock.elapsedRealtime() - applicationStartTime
                    } else {
                        0
                    },
            )
        }

        /**
         * 启动统计信息
         */
        data class StartupStats(
            val applicationStartTime: Long,
            val firstActivityCreateTime: Long,
            val firstActivityResumeTime: Long,
            val isStartupCompleted: Boolean,
            val totalDuration: Long,
        ) {
            fun getApplicationCreateDuration(): Long {
                return if (firstActivityCreateTime > 0) {
                    firstActivityCreateTime - applicationStartTime
                } else {
                    0
                }
            }

            fun getFirstActivityCreateDuration(): Long {
                return if (firstActivityResumeTime > 0 && firstActivityCreateTime > 0) {
                    firstActivityResumeTime - firstActivityCreateTime
                } else {
                    0
                }
            }
        }
    }
