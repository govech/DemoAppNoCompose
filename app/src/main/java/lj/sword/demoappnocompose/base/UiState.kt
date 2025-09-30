package lj.sword.demoappnocompose.base

/**
 * UI 状态密封类
 * 统一管理页面的各种状态
 * 
 * @param T 数据类型
 * 
 * @author Sword
 * @since 1.0.0
 */
sealed class UiState<out T> {
    /**
     * 加载中状态
     * @param message 加载提示信息
     */
    data class Loading(val message: String = "加载中...") : UiState<Nothing>()

    /**
     * 成功状态
     * @param data 成功返回的数据
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * 错误状态
     * @param code 错误码
     * @param message 错误信息
     * @param throwable 异常对象（可选）
     */
    data class Error(
        val code: Int = -1,
        val message: String,
        val throwable: Throwable? = null
    ) : UiState<Nothing>()

    /**
     * 空数据状态
     * @param message 空数据提示信息
     */
    data class Empty(val message: String = "暂无数据") : UiState<Nothing>()
}
