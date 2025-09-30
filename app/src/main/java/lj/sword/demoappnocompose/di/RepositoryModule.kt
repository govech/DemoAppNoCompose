package lj.sword.demoappnocompose.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lj.sword.demoappnocompose.data.local.DataStoreManager
import lj.sword.demoappnocompose.data.remote.ApiService
import lj.sword.demoappnocompose.data.repository.LoginRepository
import javax.inject.Singleton

/**
 * Repository 依赖注入模块
 * 提供 Repository 实例
 * 
 * @author Sword
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * 提供 LoginRepository 实例
     */
    @Provides
    @Singleton
    fun provideLoginRepository(
        apiService: ApiService,
        dataStoreManager: DataStoreManager
    ): LoginRepository {
        return LoginRepository(apiService, dataStoreManager)
    }

    // 更多 Repository 可以在这里添加
}
