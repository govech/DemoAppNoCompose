package lj.sword.demoappnocompose.domain.usecase

import kotlinx.coroutines.flow.Flow
import lj.sword.demoappnocompose.domain.model.User
import lj.sword.demoappnocompose.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 更新用户信息用例
 * 封装用户信息更新的业务逻辑
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return Flow<User> 更新后的用户信息流
     */
    suspend operator fun invoke(user: User): Flow<User> {
        // 业务逻辑验证
        validateUserInfo(user)
        
        // 执行更新
        return userRepository.updateUser(user)
    }

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return Flow<User> 更新后的用户信息流
     */
    suspend fun updateAvatar(userId: String, avatarUrl: String): Flow<User> {
        require(userId.isNotBlank()) { "用户ID不能为空" }
        require(avatarUrl.isNotBlank()) { "头像URL不能为空" }
        
        return userRepository.updateUserAvatar(userId, avatarUrl)
    }

    /**
     * 退出登录
     */
    suspend fun logout() {
        userRepository.logout()
    }

    /**
     * 验证用户信息
     * @param user 用户信息
     */
    private fun validateUserInfo(user: User) {
        require(user.id.isNotBlank()) { "用户ID不能为空" }
        require(user.username.isNotBlank()) { "用户名不能为空" }
        require(user.username.length >= 3) { "用户名长度不能少于3位" }
        
        // 验证邮箱格式（如果提供）
        if (user.email.isNotBlank()) {
            val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            require(user.email.matches(emailPattern.toRegex())) { "邮箱格式不正确" }
        }
        
        // 验证手机号格式（如果提供）
        if (user.phone.isNotBlank()) {
            val phonePattern = "^1[3-9]\\d{9}$"
            require(user.phone.matches(phonePattern.toRegex())) { "手机号格式不正确" }
        }
    }
}