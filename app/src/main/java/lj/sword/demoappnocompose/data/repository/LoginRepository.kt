package lj.sword.demoappnocompose.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import lj.sword.demoappnocompose.BuildConfig
import lj.sword.demoappnocompose.base.BaseRepository
import lj.sword.demoappnocompose.data.local.DataStoreManager
import lj.sword.demoappnocompose.data.model.ApiException
import lj.sword.demoappnocompose.data.model.LoginRequest
import lj.sword.demoappnocompose.data.model.LoginResponse
import lj.sword.demoappnocompose.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 登录仓库
 * 负责登录相关的数据操作
 *
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class LoginRepository
    @Inject
    constructor(
        private val apiService: ApiService,
        private val dataStoreManager: DataStoreManager,
    ) : BaseRepository() {
        /**
         * 是否使用 Mock 数据（用于演示）
         * 正式环境请设置为 false
         */
        private val useMockData = true

        /**
         * 用户登录
         * @param username 用户名
         * @param password 密码
         * @return Flow<LoginResponse>
         */
        fun login(
            username: String,
            password: String,
        ): Flow<LoginResponse> {
            return if (useMockData && BuildConfig.DEBUG) {
                // 使用 Mock 数据（用于演示）
                loginWithMockData(username, password)
            } else {
                // 真实网络请求
                executeRequest {
                    apiService.login(LoginRequest(username, password))
                }
            }
        }

        /**
         * Mock 数据登录（仅用于演示和测试）
         */
        private fun loginWithMockData(
            username: String,
            password: String,
        ): Flow<LoginResponse> =
            flow {
                // 模拟网络延迟
                delay(1500)

                // 模拟登录逻辑
                when {
                    username.isEmpty() || password.isEmpty() -> {
                        throw ApiException(ApiException.CODE_SERVER_ERROR, "用户名或密码不能为空")
                    }
                    username == "admin" && password == "123456" -> {
                        // 登录成功
                        emit(
                            LoginResponse(
                                userId = "1001",
                                token = "mock_token_${System.currentTimeMillis()}",
                                username = username,
                                avatar = "https://via.placeholder.com/100",
                                email = "admin@example.com",
                            ),
                        )
                    }
                    username == "test" && password == "123456" -> {
                        // 另一个测试账号
                        emit(
                            LoginResponse(
                                userId = "1002",
                                token = "mock_token_${System.currentTimeMillis()}",
                                username = username,
                                avatar = "https://via.placeholder.com/100",
                                email = "test@example.com",
                            ),
                        )
                    }
                    else -> {
                        // 登录失败
                        throw ApiException(ApiException.CODE_SERVER_ERROR, "用户名或密码错误")
                    }
                }
            }.flowOn(Dispatchers.IO)

        /**
         * 保存用户信息
         * @param response 登录响应数据
         */
        suspend fun saveUserInfo(response: LoginResponse) {
            dataStoreManager.saveToken(response.token)
            dataStoreManager.saveUserId(response.userId)
        }

        /**
         * 清除用户信息（退出登录）
         */
        suspend fun clearUserInfo() {
            dataStoreManager.clearUserData()
        }

        /**
         * 检查是否已登录
         * @return Boolean
         */
        suspend fun isLoggedIn(): Boolean {
            val token = dataStoreManager.getToken()
            return token.isNotEmpty()
        }
    }
