package lj.sword.demoappnocompose.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lj.sword.demoappnocompose.base.BaseRepository
import lj.sword.demoappnocompose.data.local.DataStoreManager
import lj.sword.demoappnocompose.data.local.dao.UserDao
import lj.sword.demoappnocompose.data.remote.ApiService
import lj.sword.demoappnocompose.domain.mapper.toDomainUser
import lj.sword.demoappnocompose.domain.mapper.toLoginResponse
import lj.sword.demoappnocompose.domain.mapper.toUserEntity
import lj.sword.demoappnocompose.domain.model.User
import lj.sword.demoappnocompose.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户仓库实现类
 * 实现Domain层定义的UserRepository接口
 *
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class UserRepositoryImpl
    @Inject
    constructor(
        private val apiService: ApiService,
        private val dataStoreManager: DataStoreManager,
        private val userDao: UserDao,
        private val loginRepository: LoginRepository,
    ) : BaseRepository(),
        UserRepository {
        override suspend fun login(
            username: String,
            password: String,
        ): Flow<User> =
            loginRepository
                .login(username, password)
                .map { loginResponse ->
                    val user = loginResponse.toDomainUser()
                    // 保存用户信息到本地
                    saveUserToLocal(user)
                    // 保存登录状态
                    loginRepository.saveUserInfo(loginResponse)
                    user
                }

        override suspend fun getCurrentUser(): Flow<User?> =
            executeLocal {
                getUserFromLocal()
            }

        override suspend fun getUserById(userId: String): Flow<User> =
            executeLocal {
                val userEntity = userDao.getUserById(userId)
                userEntity?.toDomainUser() ?: throw IllegalStateException("用户不存在")
            }

        override suspend fun updateUser(user: User): Flow<User> =
            executeRequest {
                // 这里应该调用真实的API，目前使用mock数据
                apiService.updateUser(user.toLoginResponse())
            }.map { response ->
                val updatedUser = response.toDomainUser()
                // 更新本地数据
                saveUserToLocal(updatedUser)
                updatedUser
            }

        override suspend fun updateUserAvatar(
            userId: String,
            avatarUrl: String,
        ): Flow<User> =
            executeLocal {
                val currentUser = getUserFromLocal()
                if (currentUser != null && currentUser.id == userId) {
                    val updatedUser = currentUser.copyWith(avatar = avatarUrl)
                    saveUserToLocal(updatedUser)
                    updatedUser
                } else {
                    throw IllegalStateException("用户不存在或ID不匹配")
                }
            }

        override suspend fun isLoggedIn(): Boolean = loginRepository.isLoggedIn()

        override suspend fun logout() {
            // 清除登录状态
            loginRepository.clearUserInfo()
            // 清除本地用户信息
            clearLocalUser()
        }

        override suspend fun saveUserToLocal(user: User) {
            userDao.insertUser(user.toUserEntity())
        }

        override suspend fun getUserFromLocal(): User? {
            val userId = dataStoreManager.getUserId()
            return if (userId.isNotEmpty()) {
                userDao.getUserById(userId)?.toDomainUser()
            } else {
                null
            }
        }

        override suspend fun clearLocalUser() {
            val userId = dataStoreManager.getUserId()
            if (userId.isNotEmpty()) {
                userDao.deleteUser(userId)
            }
        }
    }
