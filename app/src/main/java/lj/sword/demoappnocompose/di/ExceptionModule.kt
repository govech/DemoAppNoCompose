package lj.sword.demoappnocompose.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lj.sword.demoappnocompose.base.exception.DatabaseExceptionRecovery
import lj.sword.demoappnocompose.base.exception.ErrorMessageMapper
import lj.sword.demoappnocompose.base.exception.ExceptionHandler
import lj.sword.demoappnocompose.base.exception.NetworkExceptionRecovery
import javax.inject.Singleton

/**
 * 异常处理依赖注入模块
 * 提供异常处理相关的实例
 *
 * @author Sword
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
object ExceptionModule {
    /**
     * 提供网络异常恢复策略
     */
    @Provides
    @Singleton
    fun provideNetworkExceptionRecovery(
        @ApplicationContext context: Context,
    ): NetworkExceptionRecovery {
        return NetworkExceptionRecovery(context)
    }

    /**
     * 提供数据库异常恢复策略
     */
    @Provides
    @Singleton
    fun provideDatabaseExceptionRecovery(
        @ApplicationContext context: Context,
    ): DatabaseExceptionRecovery {
        return DatabaseExceptionRecovery(context)
    }

    /**
     * 提供异常处理器
     */
    @Provides
    @Singleton
    fun provideExceptionHandler(
        networkRecovery: NetworkExceptionRecovery,
        databaseRecovery: DatabaseExceptionRecovery,
    ): ExceptionHandler {
        return ExceptionHandler(networkRecovery, databaseRecovery)
    }

    /**
     * 提供错误消息映射器
     */
    @Provides
    @Singleton
    fun provideErrorMessageMapper(
        @ApplicationContext context: Context,
    ): ErrorMessageMapper {
        return ErrorMessageMapper(context)
    }
}
