package lj.sword.demoappnocompose.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lj.sword.demoappnocompose.data.model.ApiException

/**
 * ViewModel 基类
 * 提供统一的 Loading 状态管理、异常捕获、协程封装
 * 
 * @author Sword
 * @since 1.0.0
 */
abstract class BaseViewModel : ViewModel() {

    // Loading 状态
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * 全局异常处理器
     */
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
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
        viewModelScope.launch(exceptionHandler) {
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
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }

    /**
     * 处理异常
     * 可在子类中重写以实现自定义异常处理
     */
    protected open fun handleException(throwable: Throwable) {
        _loading.value = false
        
        val errorMessage = when (throwable) {
            is ApiException -> throwable.msg
            else -> throwable.message ?: "未知错误"
        }
        
        _error.value = errorMessage
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
}
