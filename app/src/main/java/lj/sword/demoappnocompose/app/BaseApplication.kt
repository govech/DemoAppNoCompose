package lj.sword.demoappnocompose.app

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import lj.sword.demoappnocompose.BuildConfig

/**
 * Application 基类
 * 集成 Hilt 依赖注入框架
 * 初始化全局配置和第三方库
 * 
 * @author Sword
 * @since 1.0.0
 */
@HiltAndroidApp
class BaseApplication : Application() {

    companion object {
        /**
         * 全局 Application 实例
         */
        @JvmStatic
        lateinit var instance: BaseApplication
            private set

        /**
         * 全局 Context
         */
        @JvmStatic
        val context: Context
            get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 初始化日志系统
        initLogger()
        
        // 初始化全局异常捕获
        initCrashHandler()
        
        // 初始化埋点系统
        initTracker()
        
        // 初始化网络状态监听
        initNetworkMonitor()
    }

    /**
     * 初始化日志系统
     */
    private fun initLogger() {
        lj.sword.demoappnocompose.manager.Logger.isEnabled = BuildConfig.DEBUG
        lj.sword.demoappnocompose.manager.Logger.d("Application initialized")
    }

    /**
     * 初始化全局异常捕获
     */
    private fun initCrashHandler() {
        lj.sword.demoappnocompose.manager.CrashHandler.getInstance().init(this)
    }

    /**
     * 初始化埋点系统
     */
    private fun initTracker() {
        lj.sword.demoappnocompose.manager.TrackManager.init(
            lj.sword.demoappnocompose.manager.DefaultTracker()
        )
    }

    /**
     * 初始化网络状态监听
     */
    private fun initNetworkMonitor() {
        // 可选：监听网络状态变化
        // NetworkUtil.registerNetworkCallback(this, onAvailable = {}, onLost = {})
    }
}
