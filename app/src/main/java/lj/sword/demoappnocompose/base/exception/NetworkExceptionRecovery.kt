package lj.sword.demoappnocompose.base.exception

import android.content.Context
import kotlinx.coroutines.delay
import lj.sword.demoappnocompose.manager.Logger
import lj.sword.demoappnocompose.utils.NetworkUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络异常恢复策略
 * 处理网络相关异常的恢复逻辑
 *
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class NetworkExceptionRecovery
    @Inject
    constructor(
        private val context: Context,
    ) : ExceptionRecoveryStrategy {
        companion object {
            private const val MAX_RETRY_COUNT = 3
            private const val RETRY_DELAY_BASE = 1000L
        }

        override fun canHandle(exception: Throwable): Boolean {
            return exception is AppException.NetworkException
        }

        override suspend fun recover(
            exception: Throwable,
            retryAction: suspend () -> Unit,
        ): RecoveryResult {
            if (!canHandle(exception)) {
                return RecoveryResult.NonRetryableFailure("不支持的异常类型")
            }

            val networkException = exception as AppException.NetworkException
            Logger.d("NetworkExceptionRecovery", "处理网络异常: ${networkException.javaClass.simpleName}")

            return when (networkException) {
                is AppException.NetworkException.NoNetworkError -> {
                    handleNoNetworkError()
                }
                is AppException.NetworkException.ConnectionError -> {
                    handleConnectionError(retryAction)
                }
                is AppException.NetworkException.TimeoutError -> {
                    handleTimeoutError(retryAction)
                }
                is AppException.NetworkException.ServerError -> {
                    handleServerError(networkException, retryAction)
                }
                is AppException.NetworkException.HttpError -> {
                    handleHttpError(networkException)
                }
                is AppException.NetworkException.ParseError -> {
                    handleParseError(networkException)
                }
            }
        }

        /**
         * 处理无网络连接错误
         */
        private suspend fun handleNoNetworkError(): RecoveryResult {
            return if (NetworkUtil.isNetworkAvailable(context)) {
                // 网络已恢复，可以重试
                RecoveryResult.RetryableFailure(
                    message = "网络连接已恢复，正在重试...",
                    retryDelay = 500L,
                )
            } else {
                // 网络仍然不可用，需要用户检查网络
                RecoveryResult.RequiresUserIntervention(
                    message = "网络不可用，请检查网络连接",
                    actionType = UserActionType.CHECK_NETWORK,
                )
            }
        }

        /**
         * 处理连接错误
         */
        private suspend fun handleConnectionError(retryAction: suspend () -> Unit): RecoveryResult {
            return if (NetworkUtil.isNetworkAvailable(context)) {
                // 有网络但连接失败，尝试重试
                try {
                    delay(RETRY_DELAY_BASE)
                    retryAction()
                    RecoveryResult.Success
                } catch (e: Exception) {
                    RecoveryResult.RetryableFailure(
                        message = "连接失败，正在重试...",
                        retryDelay = RETRY_DELAY_BASE * 2,
                    )
                }
            } else {
                RecoveryResult.RequiresUserIntervention(
                    message = "网络连接失败，请检查网络设置",
                    actionType = UserActionType.CHECK_NETWORK,
                )
            }
        }

        /**
         * 处理超时错误
         */
        private suspend fun handleTimeoutError(retryAction: suspend () -> Unit): RecoveryResult {
            return try {
                delay(RETRY_DELAY_BASE)
                retryAction()
                RecoveryResult.Success
            } catch (e: Exception) {
                RecoveryResult.RetryableFailure(
                    message = "请求超时，正在重试...",
                    retryDelay = RETRY_DELAY_BASE * 2,
                )
            }
        }

        /**
         * 处理服务器错误
         */
        private suspend fun handleServerError(
            exception: AppException.NetworkException.ServerError,
            retryAction: suspend () -> Unit,
        ): RecoveryResult {
            return when (exception.code) {
                in 500..599 -> {
                    // 服务器内部错误，可以重试
                    try {
                        delay(RETRY_DELAY_BASE * 3)
                        retryAction()
                        RecoveryResult.Success
                    } catch (e: Exception) {
                        RecoveryResult.RetryableFailure(
                            message = "服务器暂时不可用，正在重试...",
                            retryDelay = RETRY_DELAY_BASE * 5,
                        )
                    }
                }
                else -> {
                    RecoveryResult.NonRetryableFailure("服务器错误: ${exception.message}")
                }
            }
        }

        /**
         * 处理HTTP错误
         */
        private suspend fun handleHttpError(exception: AppException.NetworkException.HttpError): RecoveryResult {
            return when (exception.code) {
                401 -> {
                    // 未授权，需要重新登录
                    RecoveryResult.RequiresUserIntervention(
                        message = "登录已过期，请重新登录",
                        actionType = UserActionType.RE_LOGIN,
                    )
                }
                403 -> {
                    // 禁止访问
                    RecoveryResult.NonRetryableFailure("权限不足，无法访问")
                }
                404 -> {
                    // 资源不存在
                    RecoveryResult.NonRetryableFailure("请求的资源不存在")
                }
                429 -> {
                    // 请求过于频繁
                    RecoveryResult.RetryableFailure(
                        message = "请求过于频繁，请稍后重试",
                        retryDelay = RETRY_DELAY_BASE * 10,
                    )
                }
                in 500..599 -> {
                    // 服务器错误，可以重试
                    RecoveryResult.RetryableFailure(
                        message = "服务器错误，正在重试...",
                        retryDelay = RETRY_DELAY_BASE * 3,
                    )
                }
                else -> {
                    RecoveryResult.NonRetryableFailure("HTTP错误: ${exception.code}")
                }
            }
        }

        /**
         * 处理解析错误
         */
        private suspend fun handleParseError(exception: AppException.NetworkException.ParseError): RecoveryResult {
            Logger.e("数据解析失败", exception, "NetworkExceptionRecovery")
            return RecoveryResult.NonRetryableFailure("数据格式错误，请联系客服")
        }
    }
