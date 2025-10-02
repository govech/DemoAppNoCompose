package lj.sword.demoappnocompose.base.exception

/**
 * 异常恢复策略接口
 * 定义异常恢复的通用接口
 * 
 * @author Sword
 * @since 1.0.0
 */
interface ExceptionRecoveryStrategy {
    
    /**
     * 判断是否可以处理该异常
     * @param exception 异常
     * @return 是否可以处理
     */
    fun canHandle(exception: Throwable): Boolean
    
    /**
     * 恢复异常
     * @param exception 异常
     * @param retryAction 重试操作
     * @return 恢复结果
     */
    suspend fun recover(
        exception: Throwable,
        retryAction: suspend () -> Unit = {}
    ): RecoveryResult
}

/**
 * 恢复结果
 */
sealed class RecoveryResult {
    /** 恢复成功 */
    object Success : RecoveryResult()
    
    /** 恢复失败，但可以重试 */
    data class RetryableFailure(
        val message: String,
        val retryDelay: Long = 1000L
    ) : RecoveryResult()
    
    /** 恢复失败，不可重试 */
    data class NonRetryableFailure(
        val message: String
    ) : RecoveryResult()
    
    /** 需要用户干预 */
    data class RequiresUserIntervention(
        val message: String,
        val actionType: UserActionType
    ) : RecoveryResult()
}

/**
 * 用户操作类型
 */
enum class UserActionType {
    /** 重新登录 */
    RE_LOGIN,
    /** 检查网络 */
    CHECK_NETWORK,
    /** 清理存储空间 */
    CLEAN_STORAGE,
    /** 授予权限 */
    GRANT_PERMISSION,
    /** 联系客服 */
    CONTACT_SUPPORT
}