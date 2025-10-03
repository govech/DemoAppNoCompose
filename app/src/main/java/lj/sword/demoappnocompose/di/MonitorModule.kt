package lj.sword.demoappnocompose.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lj.sword.demoappnocompose.config.AppConfig
import lj.sword.demoappnocompose.monitor.MemoryMonitor
import lj.sword.demoappnocompose.monitor.NetworkPerformanceInterceptor
import lj.sword.demoappnocompose.monitor.PerformanceMonitor
import lj.sword.demoappnocompose.monitor.StartupTimeTracker
import javax.inject.Singleton

/**
 * 性能监控依赖注入模块
 * 提供性能监控相关的实例
 *
 * @author Sword
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
object MonitorModule {
    /**
     * 提供启动时间追踪器
     */
    @Provides
    @Singleton
    fun provideStartupTimeTracker(appConfig: AppConfig): StartupTimeTracker {
        return StartupTimeTracker(appConfig)
    }

    /**
     * 提供内存监控器
     */
    @Provides
    @Singleton
    fun provideMemoryMonitor(
        @ApplicationContext context: Context,
        appConfig: AppConfig,
    ): MemoryMonitor {
        return MemoryMonitor(context, appConfig)
    }

    /**
     * 提供网络性能拦截器
     */
    @Provides
    @Singleton
    fun provideNetworkPerformanceInterceptor(appConfig: AppConfig): NetworkPerformanceInterceptor {
        return NetworkPerformanceInterceptor(appConfig)
    }

    /**
     * 提供性能监控管理器
     */
    @Provides
    @Singleton
    fun providePerformanceMonitor(
        @ApplicationContext context: Context,
        appConfig: AppConfig,
        startupTimeTracker: StartupTimeTracker,
        memoryMonitor: MemoryMonitor,
        networkPerformanceInterceptor: NetworkPerformanceInterceptor,
    ): PerformanceMonitor {
        return PerformanceMonitor(
            context,
            appConfig,
            startupTimeTracker,
            memoryMonitor,
            networkPerformanceInterceptor,
        )
    }
}
