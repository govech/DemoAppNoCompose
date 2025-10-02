package lj.sword.demoappnocompose.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lj.sword.demoappnocompose.config.AppConfig
import lj.sword.demoappnocompose.config.AppConfigProvider
import javax.inject.Singleton

/**
 * 配置依赖注入模块
 * 提供应用配置相关的实例
 * 
 * @author Sword
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    /**
     * 提供应用配置提供者
     */
    @Provides
    @Singleton
    fun provideAppConfigProvider(@ApplicationContext context: Context): AppConfigProvider {
        return AppConfigProvider(context)
    }

    /**
     * 提供应用配置实例
     */
    @Provides
    @Singleton
    fun provideAppConfig(provider: AppConfigProvider): AppConfig {
        return provider.getConfig()
    }
}