package lj.sword.demoappnocompose.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

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

    // Repository 实例将在创建具体 Repository 后添加
    // @Provides
    // @Singleton
    // fun provideUserRepository(
    //     apiService: ApiService,
    //     userDao: UserDao
    // ): UserRepository {
    //     return UserRepositoryImpl(apiService, userDao)
    // }
}
