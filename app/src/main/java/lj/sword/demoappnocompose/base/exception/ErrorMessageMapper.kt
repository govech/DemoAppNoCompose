package lj.sword.demoappnocompose.base.exception

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 错误消息映射器
 * 将异常转换为用户友好的本地化错误消息
 *
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class ErrorMessageMapper
    @Inject
    constructor(
        private val context: Context,
    ) {
        /**
         * 获取用户友好的错误消息
         * @param exception 异常
         * @return 本地化的错误消息
         */
        fun getErrorMessage(exception: Throwable): String {
            return when (exception) {
                // 网络异常
                is AppException.NetworkException.NoNetworkError ->
                    "网络不可用，请检查网络连接"
                is AppException.NetworkException.TimeoutError ->
                    "网络请求超时，请稍后重试"
                is AppException.NetworkException.ConnectionError ->
                    "网络连接失败，请检查网络设置"
                is AppException.NetworkException.ServerError ->
                    "服务器暂时不可用，请稍后重试"
                is AppException.NetworkException.ParseError ->
                    "数据解析失败，请稍后重试"

                // 业务异常
                is AppException.BusinessException.NotLoginError ->
                    "请先登录"
                is AppException.BusinessException.PermissionDeniedError ->
                    "权限不足，无法执行此操作"
                is AppException.BusinessException.InvalidParameterError ->
                    "参数错误"
                is AppException.BusinessException.UserNotFoundError ->
                    "用户不存在"
                is AppException.BusinessException.WrongPasswordError ->
                    "密码错误"
                is AppException.BusinessException.AccountLockedError ->
                    "账号已被锁定，请联系管理员"
                is AppException.BusinessException.InvalidVerificationCodeError ->
                    "验证码错误或已过期"

                // 数据库异常
                is AppException.DatabaseException.ConnectionError ->
                    "数据库连接失败"
                is AppException.DatabaseException.InsertError ->
                    "数据保存失败"
                is AppException.DatabaseException.QueryError ->
                    "数据查询失败"
                is AppException.DatabaseException.UpdateError ->
                    "数据更新失败"
                is AppException.DatabaseException.DeleteError ->
                    "数据删除失败"

                // 系统异常
                is AppException.SystemException.OutOfMemoryError ->
                    "内存不足，请关闭其他应用后重试"
                is AppException.SystemException.InsufficientStorageError ->
                    "存储空间不足，请清理后重试"
                is AppException.SystemException.FileOperationError ->
                    "文件操作失败"
                is AppException.SystemException.PermissionError ->
                    "系统权限被拒绝"

                // HTTP错误
                is AppException.NetworkException.HttpError -> {
                    when (exception.code) {
                        400 -> "请求参数错误"
                        401 -> "登录已过期，请重新登录"
                        403 -> "权限不足，无法访问"
                        404 -> "请求的资源不存在"
                        429 -> "请求过于频繁，请稍后重试"
                        500 -> "服务器内部错误"
                        502 -> "网关错误"
                        503 -> "服务暂时不可用"
                        else -> "网络请求失败"
                    }
                }

                // 其他异常
                else -> {
                    if (exception is AppException) {
                        exception.getUserFriendlyMessage()
                    } else {
                        "未知错误，请稍后重试"
                    }
                }
            }
        }

        /**
         * 获取字符串资源，如果资源不存在则返回默认值
         */
        private fun getStringResource(
            resId: Int,
            defaultValue: String,
        ): String {
            return try {
                context.getString(resId)
            } catch (e: Exception) {
                defaultValue
            }
        }

        /**
         * 获取操作建议
         * @param exception 异常
         * @return 操作建议文本
         */
        fun getActionSuggestion(exception: Throwable): String? {
            return when (exception) {
                is AppException.NetworkException.NoNetworkError ->
                    "请检查网络连接后重试"
                is AppException.NetworkException.TimeoutError ->
                    "请稍后重试"
                is AppException.BusinessException.NotLoginError ->
                    "请重新登录"
                is AppException.SystemException.InsufficientStorageError ->
                    "请清理存储空间后重试"
                is AppException.SystemException.PermissionError ->
                    "请在设置中授予相关权限"
                else -> null
            }
        }
    }
