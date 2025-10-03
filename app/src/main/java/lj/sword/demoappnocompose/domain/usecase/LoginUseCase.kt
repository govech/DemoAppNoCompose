package lj.sword.demoappnocompose.domain.usecase

import kotlinx.coroutines.flow.Flow
import lj.sword.demoappnocompose.domain.model.User
import lj.sword.demoappnocompose.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 登录用例
 * 封装登录相关的业务逻辑
 *
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class LoginUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        /**
         * 执行登录
         * @param username 用户名
         * @param password 密码
         * @return Flow<User> 用户信息流
         */
        suspend operator fun invoke(
            username: String,
            password: String,
        ): Flow<User> {
            // 业务逻辑验证
            validateLoginInput(username, password)

            // 执行登录并返回用户信息
            return userRepository.login(username, password)
        }

        /**
         * 验证登录输入
         * @param username 用户名
         * @param password 密码
         */
        private fun validateLoginInput(
            username: String,
            password: String,
        ) {
            require(username.isNotBlank()) { "用户名不能为空" }
            require(password.isNotBlank()) { "密码不能为空" }
            require(username.length >= 3) { "用户名长度不能少于3位" }
            require(password.length >= 6) { "密码长度不能少于6位" }
        }
    }
