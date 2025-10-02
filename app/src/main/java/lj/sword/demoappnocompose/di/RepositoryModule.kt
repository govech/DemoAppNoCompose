package lj.sword.demoappnocompose.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lj.sword.demoappnocompose.data.local.DataStoreManager
import lj.sword.demoappnocompose.data.local.dao.UserDao
import lj.sword.demoappnocompose.data.remote.ApiService
import lj.sword.demoappnocompose.data.repository.LoginRepository
import lj.sword.demoappnocompose.data.repository.UserRepositoryImpl
import lj.sword.demoappnocompose.domain.repository.UserRepository
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

    /**
     * 提供 UserRepositoryImpl 实例
     */
    @Provides
    @Singleton
    fun provideUserRepositoryImpl(
        apiService: ApiService,
        dataStoreManager: DataStoreManager,
        userDao: UserDao,
        loginRepository: LoginRepository
    ): UserRepositoryImpl {
        return UserRepositoryImpl(apiService, dataStoreManager, userDao, loginRepository)
    }
}

/**
 * Repository 绑定模块
 * 将接口绑定到具体实现
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindModule {

    /**
     * 绑定 UserRepository 接口到 UserRepositoryImpl 实现
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
