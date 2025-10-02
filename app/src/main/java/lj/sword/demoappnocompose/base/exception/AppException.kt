package lj.sword.demoappnocompose.base.exception

/**
 * 应用异常基类
 * 使用密封类定义应用中的所有异常类型
 * 
 * @author Sword
 * @since 1.0.0
 */
sealed class AppException(
    override val message: String,
    override val cause: Throwable? = null,
    val code: Int = 0
) : Exception(message, cause) {

    /**
     * 网络异常
     */
    sealed class NetworkException(
        message: String,
        cause: Throwable? = null,
        code: Int = 0
    ) : AppException(message, cause, code) {
        
        /** 网络连接失败 */
        class ConnectionError(cause: Throwable? = null) : NetworkException(
            message = "网络连接失败，请检查网络设置",
            cause = cause,
            code = ERROR_CODE_CONNECTION
        )
        
        /** 请求超时 */
        class TimeoutError(cause: Throwable? = null) : NetworkException(
            message = "请求超时，请稍后重试",
            cause = cause,
            code = ERROR_CODE_TIMEOUT
        )
        
        /** 服务器错误 */
        class ServerError(
            message: String = "服务器错误，请稍后重试",
            cause: Throwable? = null,
            code: Int = ERROR_CODE_SERVER
        ) : NetworkException(message, cause, code)
        
        /** HTTP错误 */
        class HttpError(
            httpCode: Int,
            message: String = "HTTP错误: $httpCode",
            cause: Throwable? = null
        ) : NetworkException(message, cause, httpCode)
        
        /** 解析错误 */
        class ParseError(cause: Throwable? = null) : NetworkException(
            message = "数据解析失败",
            cause = cause,
            code = ERROR_CODE_PARSE
        )
        
        /** 无网络连接 */
        class NoNetworkError : NetworkException(
            message = "无网络连接，请检查网络设置",
            code = ERROR_CODE_NO_NETWORK
        )
        
        companion object {
            const val ERROR_CODE_CONNECTION = 1001
            const val ERROR_CODE_TIMEOUT = 1002
            const val ERROR_CODE_SERVER = 1003
            const val ERROR_CODE_PARSE = 1004
            const val ERROR_CODE_NO_NETWORK = 1005
        }
    }

    /**
     * 数据库异常
     */
    sealed class DatabaseException(
        message: String,
        cause: Throwable? = null,
        code: Int = 0
    ) : AppException(message, cause, code) {
        
        /** 数据库连接失败 */
        class ConnectionError(cause: Throwable? = null) : DatabaseException(
            message = "数据库连接失败",
            cause = cause,
            code = ERROR_CODE_DB_CONNECTION
        )
        
        /** 数据插入失败 */
        class InsertError(cause: Throwable? = null) : DatabaseException(
            message = "数据保存失败",
            cause = cause,
            code = ERROR_CODE_DB_INSERT
        )
        
        /** 数据查询失败 */
        class QueryError(cause: Throwable? = null) : DatabaseException(
            message = "数据查询失败",
            cause = cause,
            code = ERROR_CODE_DB_QUERY
        )
        
        /** 数据更新失败 */
        class UpdateError(cause: Throwable? = null) : DatabaseException(
            message = "数据更新失败",
            cause = cause,
            code = ERROR_CODE_DB_UPDATE
        )
        
        /** 数据删除失败 */
        class DeleteError(cause: Throwable? = null) : DatabaseException(
            message = "数据删除失败",
            cause = cause,
            code = ERROR_CODE_DB_DELETE
        )
        
        /** 数据库版本迁移失败 */
        class MigrationError(cause: Throwable? = null) : DatabaseException(
            message = "数据库升级失败",
            cause = cause,
            code = ERROR_CODE_DB_MIGRATION
        )
        
        companion object {
            const val ERROR_CODE_DB_CONNECTION = 2001
            const val ERROR_CODE_DB_INSERT = 2002
            const val ERROR_CODE_DB_QUERY = 2003
            const val ERROR_CODE_DB_UPDATE = 2004
            const val ERROR_CODE_DB_DELETE = 2005
            const val ERROR_CODE_DB_MIGRATION = 2006
        }
    }

    /**
     * 业务异常
     */
    sealed class BusinessException(
        message: String,
        cause: Throwable? = null,
        code: Int = 0
    ) : AppException(message, cause, code) {
        
        /** 用户未登录 */
        class NotLoginError : BusinessException(
            message = "用户未登录，请先登录",
            code = ERROR_CODE_NOT_LOGIN
        )
        
        /** 权限不足 */
        class PermissionDeniedError(
            message: String = "权限不足，无法执行此操作"
        ) : BusinessException(message, code = ERROR_CODE_PERMISSION_DENIED)
        
        /** 参数错误 */
        class InvalidParameterError(
            message: String = "参数错误"
        ) : BusinessException(message, code = ERROR_CODE_INVALID_PARAMETER)
        
        /** 用户不存在 */
        class UserNotFoundError : BusinessException(
            message = "用户不存在",
            code = ERROR_CODE_USER_NOT_FOUND
        )
        
        /** 密码错误 */
        class WrongPasswordError : BusinessException(
            message = "密码错误",
            code = ERROR_CODE_WRONG_PASSWORD
        )
        
        /** 账号被锁定 */
        class AccountLockedError : BusinessException(
            message = "账号已被锁定，请联系管理员",
            code = ERROR_CODE_ACCOUNT_LOCKED
        )
        
        /** 验证码错误 */
        class InvalidVerificationCodeError : BusinessException(
            message = "验证码错误或已过期",
            code = ERROR_CODE_INVALID_VERIFICATION_CODE
        )
        
        /** 业务规则违反 */
        class BusinessRuleViolationError(
            message: String
        ) : BusinessException(message, code = ERROR_CODE_BUSINESS_RULE_VIOLATION)
        
        companion object {
            const val ERROR_CODE_NOT_LOGIN = 3001
            const val ERROR_CODE_PERMISSION_DENIED = 3002
            const val ERROR_CODE_INVALID_PARAMETER = 3003
            const val ERROR_CODE_USER_NOT_FOUND = 3004
            const val ERROR_CODE_WRONG_PASSWORD = 3005
            const val ERROR_CODE_ACCOUNT_LOCKED = 3006
            const val ERROR_CODE_INVALID_VERIFICATION_CODE = 3007
            const val ERROR_CODE_BUSINESS_RULE_VIOLATION = 3008
        }
    }

    /**
     * 系统异常
     */
    sealed class SystemException(
        message: String,
        cause: Throwable? = null,
        code: Int = 0
    ) : AppException(message, cause, code) {
        
        /** 内存不足 */
        class OutOfMemoryError(cause: Throwable? = null) : SystemException(
            message = "内存不足",
            cause = cause,
            code = ERROR_CODE_OUT_OF_MEMORY
        )
        
        /** 存储空间不足 */
        class InsufficientStorageError : SystemException(
            message = "存储空间不足",
            code = ERROR_CODE_INSUFFICIENT_STORAGE
        )
        
        /** 文件操作失败 */
        class FileOperationError(
            message: String = "文件操作失败",
            cause: Throwable? = null
        ) : SystemException(message, cause, ERROR_CODE_FILE_OPERATION)
        
        /** 权限被拒绝 */
        class PermissionError(
            message: String = "系统权限被拒绝"
        ) : SystemException(message, code = ERROR_CODE_SYSTEM_PERMISSION)
        
        /** 未知系统错误 */
        class UnknownError(
            message: String = "未知系统错误",
            cause: Throwable? = null
        ) : SystemException(message, cause, ERROR_CODE_UNKNOWN)
        
        companion object {
            const val ERROR_CODE_OUT_OF_MEMORY = 4001
            const val ERROR_CODE_INSUFFICIENT_STORAGE = 4002
            const val ERROR_CODE_FILE_OPERATION = 4003
            const val ERROR_CODE_SYSTEM_PERMISSION = 4004
            const val ERROR_CODE_UNKNOWN = 4999
        }
    }

    /**
     * 获取用户友好的错误消息
     */
    fun getUserFriendlyMessage(): String {
        return when (this) {
            is NetworkException.NoNetworkError -> "网络不可用，请检查网络连接"
            is NetworkException.TimeoutError -> "网络请求超时，请稍后重试"
            is NetworkException.ConnectionError -> "网络连接失败，请检查网络设置"
            is BusinessException.NotLoginError -> "请先登录"
            is BusinessException.PermissionDeniedError -> "权限不足"
            is SystemException.OutOfMemoryError -> "内存不足，请关闭其他应用后重试"
            is SystemException.InsufficientStorageError -> "存储空间不足，请清理后重试"
            else -> message
        }
    }

    /**
     * 判断是否可以重试
     */
    fun isRetryable(): Boolean {
        return when (this) {
            is NetworkException.TimeoutError,
            is NetworkException.ConnectionError,
            is NetworkException.ServerError -> true
            is DatabaseException.ConnectionError -> true
            else -> false
        }
    }

    /**
     * 获取重试延迟时间（毫秒）
     */
    fun getRetryDelay(): Long {
        return when (this) {
            is NetworkException.TimeoutError -> 2000L
            is NetworkException.ConnectionError -> 3000L
            is NetworkException.ServerError -> 5000L
            else -> 1000L
        }
    }
}