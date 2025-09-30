package lj.sword.demoappnocompose.ext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import lj.sword.demoappnocompose.base.UiState

/**
 * Flow 扩展函数
 * 统一处理 Flow 的 Loading、Error 状态
 * 
 * @author Sword
 * @since 1.0.0
 */

/**
 * Flow 转 UiState
 * 自动添加 Loading 和 Error 状态处理
 */
fun <T> Flow<T>.asUiState(): Flow<UiState<T>> {
    return kotlinx.coroutines.flow.flow {
        emit(UiState.Loading() as UiState<T>)
        this@asUiState.catch { e ->
            emit(
                UiState.Error(
                    message = e.message ?: "未知错误",
                    throwable = e
                ) as UiState<T>
            )
        }.collect { data ->
            emit(UiState.Success(data))
        }
    }
}

/**
 * 简化的错误处理
 */
fun <T> Flow<T>.catchError(
    onError: suspend (Throwable) -> Unit
): Flow<T> {
    return this.catch { e ->
        onError(e)
    }
}
