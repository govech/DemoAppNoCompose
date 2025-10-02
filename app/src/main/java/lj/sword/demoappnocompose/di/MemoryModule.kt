package lj.sword.demoappnocompose.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lj.sword.demoappnocompose.config.AppConfig
import lj.sword.demoappnocompose.memory.ImageCacheManager
import lj.sword.demoappnocompose.memory.MemoryCache
import lj.sword.demoappnocompose.memory.MemoryManager
import lj.sword.demoappnocompose.memory.NetworkObjectPool
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * 内存管理依赖注入模块
 * 提供内存管理相关的实例
 * 
 * @author Sword
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
object MemoryModule {

    /**
     * 提供内存缓存
     */
    @Provides
    @Singleton
    fun provideMemoryCache(appConfig: AppConfig): MemoryCache {
        return MemoryCache(appConfig)
    }

    /**
     * 提供网络对象池
     */
    @Provides
    @Singleton
    fun provideNetworkObjectPool(): NetworkObjectPool {
        return NetworkObjectPool()
    }

    /**
     * 提供图片缓存管理器
     */
    @Provides
    @Singleton
    fun provideImageCacheManager(
        @ApplicationContext context: Context,
        appConfig: AppConfig,
        okHttpClient: OkHttpClient
    ): ImageCacheManager {
        return ImageCacheManager(context, appConfig, okHttpClient)
    }

    /**
     * 提供内存管理器
     */
    @Provides
    @Singleton
    fun provideMemoryManager(
        appConfig: AppConfig,
        memoryCache: MemoryCache,
        networkObjectPool: NetworkObjectPool,
        imageCacheManager: ImageCacheManager
    ): MemoryManager {
        return MemoryManager(appConfig, memoryCache, networkObjectPool, imageCacheManager)
    }
}