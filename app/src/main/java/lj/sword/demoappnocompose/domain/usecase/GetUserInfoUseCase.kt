package lj.sword.demoappnocompose.domain.usecase

import kotlinx.coroutines.flow.Flow
import lj.sword.demoappnocompose.domain.model.User
import lj.sword.demoappnocompose.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 获取用户信息用例
 * 封装获取用户信息的业务逻辑
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class GetUserInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    /**
     * 获取当前用户信息
     * @return Flow<User?> 用户信息流，可能为空
     */
    suspend operator fun invoke(): Flow<User?> {
        return userRepository.getCurrentUser()
    }

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return Flow<User> 用户信息流
     */
    suspend operator fun invoke(userId: String): Flow<User> {
        require(userId.isNotBlank()) { "用户ID不能为空" }
        return userRepository.getUserById(userId)
    }

    /**
     * 检查用户是否已登录
     * @return Boolean 是否已登录
     */
    suspend fun isLoggedIn(): Boolean {
        return userRepository.isLoggedIn()
    }
}