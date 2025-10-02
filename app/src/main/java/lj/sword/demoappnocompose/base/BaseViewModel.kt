package lj.sword.demoappnocompose.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lj.sword.demoappnocompose.base.exception.AppException
import lj.sword.demoappnocompose.base.exception.ExceptionHandler
import lj.sword.demoappnocompose.data.model.ApiException
import javax.inject.Inject

/**
 * ViewModel 基类
 * 提供统一的 Loading 状态管理、异常捕获、协程封装
 * 
 * @author Sword
 * @since 1.0.0
 */
abstract class BaseViewModel : ViewModel() {

    @Inject
    lateinit var appExceptionHandler: ExceptionHandler

    // Loading 状态
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 用户操作提示
    private val _userActionRequired = MutableStateFlow<UserActionRequired?>(null)
    val userActionRequired: StateFlow<UserActionRequired?> = _userActionRequired.asStateFlow()

    /**
     * 协程异常处理器
     */
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleException(throwable)
    }

    /**
     * 启动协程（带异常处理）
     * @param showLoading 是否显示 Loading
     * @param block 协程代码块
     */
    protected fun launchWithLoading(
        showLoading: Boolean = true,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                if (showLoading) _loading.value = true
                block()
            } finally {
                if (showLoading) _loading.value = false
            }
        }
    }

    /**
     * 启动协程（不带 Loading）
     */
    protected fun launchSafely(
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(coroutineExceptionHandler) {
            block()
        }
    }

    /**
     * 处理异常
     * 使用统一的异常处理器处理异常
     */
    protected open fun handleException(throwable: Throwable) {
        _loading.value = false
        
        // 使用异常处理器获取用户友好的错误消息
        val errorMessage = if (::appExceptionHandler.isInitialized) {
            appExceptionHandler.getUserFriendlyMessage(throwable)
        } else {
            when (throwable) {
                is AppException -> throwable.getUserFriendlyMessage()
                is ApiException -> throwable.msg
                else -> throwable.message ?: "未知错误"
            }
        }
        
        _error.value = errorMessage
        
        // 检查是否需要用户操作
        if (throwable is AppException) {
            checkUserActionRequired(throwable)
        }
    }

    /**
     * 检查是否需要用户操作
     */
    private fun checkUserActionRequired(exception: AppException) {
        when (exception) {
            is AppException.BusinessException.NotLoginError -> {
                _userActionRequired.value = UserActionRequired.ReLogin("登录已过期，请重新登录")
            }
            is AppException.NetworkException.NoNetworkError -> {
                _userActionRequired.value = UserActionRequired.CheckNetwork("网络不可用，请检查网络连接")
            }
            is AppException.SystemException.InsufficientStorageError -> {
                _userActionRequired.value = UserActionRequired.CleanStorage("存储空间不足，请清理后重试")
            }
            is AppException.SystemException.PermissionError -> {
                _userActionRequired.value = UserActionRequired.GrantPermission("需要授予相关权限")
            }
            else -> {
                // 其他异常不需要特殊用户操作
            }
        }
    }

    /**
     * 显示 Loading
     */
    protected fun showLoading() {
        _loading.value = true
    }

    /**
     * 隐藏 Loading
     */
    protected fun hideLoading() {
        _loading.value = false
    }

    /**
     * 显示错误信息
     */
    protected fun showError(message: String) {
        _error.value = message
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 清除用户操作提示
     */
    fun clearUserActionRequired() {
        _userActionRequired.value = null
    }
}

/**
 * 用户操作要求
 */
sealed class UserActionRequired(val message: String) {
    class ReLogin(message: String) : UserActionRequired(message)
    class CheckNetwork(message: String) : UserActionRequired(message)
    class CleanStorage(message: String) : UserActionRequired(message)
    class GrantPermission(message: String) : UserActionRequired(message)
    class ContactSupport(message: String) : UserActionRequired(message)
}
