package lj.sword.demoappnocompose.domain.repository

import kotlinx.coroutines.flow.Flow
import lj.sword.demoappnocompose.domain.model.User

/**
 * 用户仓库接口
 * Domain层定义的用户数据访问接口，由Data层实现
 * 
 * @author Sword
 * @since 1.0.0
 */
interface UserRepository {

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return Flow<User> 用户信息流
     */
    suspend fun login(username: String, password: String): Flow<User>

    /**
     * 获取当前登录用户信息
     * @return Flow<User?> 用户信息流，可能为空
     */
    suspend fun getCurrentUser(): Flow<User?>

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return Flow<User> 用户信息流
     */
    suspend fun getUserById(userId: String): Flow<User>

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return Flow<User> 更新后的用户信息流
     */
    suspend fun updateUser(user: User): Flow<User>

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return Flow<User> 更新后的用户信息流
     */
    suspend fun updateUserAvatar(userId: String, avatarUrl: String): Flow<User>

    /**
     * 检查用户是否已登录
     * @return Boolean 是否已登录
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * 退出登录
     */
    suspend fun logout()

    /**
     * 保存用户信息到本地
     * @param user 用户信息
     */
    suspend fun saveUserToLocal(user: User)

    /**
     * 从本地获取用户信息
     * @return User? 用户信息，可能为空
     */
    suspend fun getUserFromLocal(): User?

    /**
     * 清除本地用户信息
     */
    suspend fun clearLocalUser()
}