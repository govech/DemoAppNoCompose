package lj.sword.demoappnocompose.ui.activity

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import lj.sword.demoappnocompose.base.BaseViewModel
import lj.sword.demoappnocompose.base.UiState
import lj.sword.demoappnocompose.domain.model.User
import lj.sword.demoappnocompose.domain.usecase.GetUserInfoUseCase
import lj.sword.demoappnocompose.domain.usecase.LoginUseCase
import javax.inject.Inject

/**
 * 登录 ViewModel
 * 使用Domain层的UseCase处理业务逻辑
 *
 * @author Sword
 * @since 1.0.0
 */
@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val loginUseCase: LoginUseCase,
        private val getUserInfoUseCase: GetUserInfoUseCase,
    ) : BaseViewModel() {
        private val _loginResult = MutableStateFlow<UiState<User>>(UiState.Empty())
        val loginResult: StateFlow<UiState<User>> = _loginResult.asStateFlow()

        private val _isLoggedIn = MutableStateFlow(false)
        val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

        init {
            // 检查登录状态
            checkLoginStatus()
        }

        /**
         * 用户登录
         * @param username 用户名
         * @param password 密码
         */
        fun login(
            username: String,
            password: String,
        ) {
            launchWithLoading {
                try {
                    _loginResult.value = UiState.Loading("正在登录...")

                    loginUseCase(username, password)
                        .catch { e ->
                            _loginResult.value = UiState.Error(message = e.message ?: "登录失败")
                        }
                        .collect { user ->
                            // 更新登录状态
                            _isLoggedIn.value = true
                            _loginResult.value = UiState.Success(user)
                        }
                } catch (e: Exception) {
                    _loginResult.value = UiState.Error(message = e.message ?: "登录失败")
                }
            }
        }

        /**
         * 检查登录状态
         */
        private fun checkLoginStatus() {
            launchSafely {
                _isLoggedIn.value = getUserInfoUseCase.isLoggedIn()
            }
        }

        /**
         * 获取当前用户信息
         */
        fun getCurrentUser() {
            launchSafely {
                getUserInfoUseCase()
                    .catch { e ->
                        // 获取用户信息失败，可能未登录
                        _isLoggedIn.value = false
                    }
                    .collect { user ->
                        if (user != null) {
                            _isLoggedIn.value = true
                            _loginResult.value = UiState.Success(user)
                        } else {
                            _isLoggedIn.value = false
                        }
                    }
            }
        }

        /**
         * 重置登录状态
         */
        fun resetLoginState() {
            _loginResult.value = UiState.Empty()
        }
    }
