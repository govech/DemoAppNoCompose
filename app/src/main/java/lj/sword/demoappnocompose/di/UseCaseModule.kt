package lj.sword.demoappnocompose.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lj.sword.demoappnocompose.domain.repository.UserRepository
import lj.sword.demoappnocompose.domain.usecase.GetUserInfoUseCase
import lj.sword.demoappnocompose.domain.usecase.LoginUseCase
import lj.sword.demoappnocompose.domain.usecase.UpdateUserUseCase
import javax.inject.Singleton

/**
 * UseCase 依赖注入模块
 * 提供所有 UseCase 实例
 * 
 * @author Sword
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    /**
     * 提供 LoginUseCase 实例
     */
    @Provides
    @Singleton
    fun provideLoginUseCase(
        userRepository: UserRepository
    ): LoginUseCase {
        return LoginUseCase(userRepository)
    }

    /**
     * 提供 GetUserInfoUseCase 实例
     */
    @Provides
    @Singleton
    fun provideGetUserInfoUseCase(
        userRepository: UserRepository
    ): GetUserInfoUseCase {
        return GetUserInfoUseCase(userRepository)
    }

    /**
     * 提供 UpdateUserUseCase 实例
     */
    @Provides
    @Singleton
    fun provideUpdateUserUseCase(
        userRepository: UserRepository
    ): UpdateUserUseCase {
        return UpdateUserUseCase(userRepository)
    }
}