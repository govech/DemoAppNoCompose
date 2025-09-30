package lj.sword.demoappnocompose.ui.activity

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import lj.sword.demoappnocompose.base.BaseViewModel
import lj.sword.demoappnocompose.base.UiState
import lj.sword.demoappnocompose.data.model.LoginResponse
import lj.sword.demoappnocompose.data.repository.LoginRepository
import javax.inject.Inject

/**
 * 登录 ViewModel
 * 
 * @author Sword
 * @since 1.0.0
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : BaseViewModel() {

    private val _loginResult = MutableStateFlow<UiState<LoginResponse>>(UiState.Empty())
    val loginResult: StateFlow<UiState<LoginResponse>> = _loginResult.asStateFlow()

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     */
    fun login(username: String, password: String) {
        launchWithLoading {
            _loginResult.value = UiState.Loading("正在登录...")
            
            loginRepository.login(username, password)
                .catch { e ->
                    _loginResult.value = UiState.Error(message = e.message ?: "登录失败")
                }
                .collect { response ->
                    // 保存用户信息
                    loginRepository.saveUserInfo(response)
                    
                    // 更新状态
                    _loginResult.value = UiState.Success(response)
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
