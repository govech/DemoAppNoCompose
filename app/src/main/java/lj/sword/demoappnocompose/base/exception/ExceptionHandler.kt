package lj.sword.demoappnocompose.base.exception

import kotlinx.coroutines.delay
import lj.sword.demoappnocompose.manager.Logger
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 异常处理器
 * 统一处理应用中的异常，提供异常转换和恢复功能
 *
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class ExceptionHandler
    @Inject
    constructor(
        private val networkRecovery: NetworkExceptionRecovery,
        private val databaseRecovery: DatabaseExceptionRecovery,
    ) {
        companion object {
            private const val MAX_RETRY_COUNT = 3
        }

        private val recoveryStrategies =
            listOf(
                networkRecovery,
                databaseRecovery,
            )

        /**
         * 处理异常
         * @param exception 原始异常
         * @param retryAction 重试操作
         * @param maxRetryCount 最大重试次数
         * @return 处理后的异常
         */
        suspend fun handleException(
            exception: Throwable,
            retryAction: suspend () -> Unit = {},
            maxRetryCount: Int = MAX_RETRY_COUNT,
        ): AppException {
            Logger.e("处理异常: ${exception.javaClass.simpleName}", exception, "ExceptionHandler")

            // 1. 转换为应用异常
            val appException = convertToAppException(exception)

            // 2. 尝试恢复异常
            val recoveryResult = tryRecover(appException, retryAction, maxRetryCount)

            // 3. 根据恢复结果决定最终异常
            return when (recoveryResult) {
                is RecoveryResult.Success -> {
                    Logger.d("ExceptionHandler", "异常恢复成功")
                    appException // 虽然恢复成功，但仍返回原异常供调用方了解情况
                }
                is RecoveryResult.RetryableFailure -> {
                    Logger.d("ExceptionHandler", "异常恢复失败，但可重试: ${recoveryResult.message}")
                    appException
                }
                is RecoveryResult.NonRetryableFailure -> {
                    Logger.d("ExceptionHandler", "异常恢复失败，不可重试: ${recoveryResult.message}")
                    appException
                }
                is RecoveryResult.RequiresUserIntervention -> {
                    Logger.d("ExceptionHandler", "异常需要用户干预: ${recoveryResult.message}")
                    appException
                }
            }
        }

        /**
         * 转换为应用异常
         */
        private fun convertToAppException(exception: Throwable): AppException {
            return when (exception) {
                is AppException -> exception

                // 网络异常转换
                is UnknownHostException -> AppException.NetworkException.NoNetworkError()
                is ConnectException -> AppException.NetworkException.ConnectionError(exception)
                is SocketTimeoutException -> AppException.NetworkException.TimeoutError(exception)
                is IOException -> AppException.NetworkException.ConnectionError(exception)

                // 业务异常转换
                is IllegalArgumentException ->
                    AppException.BusinessException.InvalidParameterError(
                        exception.message ?: "参数错误",
                    )
                is IllegalStateException ->
                    AppException.BusinessException.BusinessRuleViolationError(
                        exception.message ?: "业务规则违反",
                    )

                // 系统异常转换
                is OutOfMemoryError -> AppException.SystemException.OutOfMemoryError(exception)
                is SecurityException ->
                    AppException.SystemException.PermissionError(
                        exception.message ?: "系统权限被拒绝",
                    )

                // 其他异常
                else ->
                    AppException.SystemException.UnknownError(
                        exception.message ?: "未知错误",
                        exception,
                    )
            }
        }

        /**
         * 尝试恢复异常
         */
        private suspend fun tryRecover(
            exception: AppException,
            retryAction: suspend () -> Unit,
            maxRetryCount: Int,
        ): RecoveryResult {
            // 找到合适的恢复策略
            val strategy =
                recoveryStrategies.find { it.canHandle(exception) }
                    ?: return RecoveryResult.NonRetryableFailure("没有找到合适的恢复策略")

            var retryCount = 0
            var lastResult: RecoveryResult = RecoveryResult.NonRetryableFailure("未开始恢复")

            while (retryCount < maxRetryCount) {
                try {
                    lastResult = strategy.recover(exception, retryAction)

                    when (lastResult) {
                        is RecoveryResult.Success -> {
                            return lastResult
                        }
                        is RecoveryResult.RetryableFailure -> {
                            retryCount++
                            if (retryCount < maxRetryCount) {
                                Logger.d("ExceptionHandler", "恢复失败，第${retryCount}次重试，延迟${lastResult.retryDelay}ms")
                                delay(lastResult.retryDelay)
                                continue
                            }
                        }
                        is RecoveryResult.NonRetryableFailure,
                        is RecoveryResult.RequiresUserIntervention,
                        -> {
                            return lastResult
                        }
                    }
                } catch (e: Exception) {
                    Logger.e("恢复策略执行失败", e, "ExceptionHandler")
                    retryCount++
                    if (retryCount < maxRetryCount) {
                        delay(1000L)
                        continue
                    }
                }
            }

            return lastResult
        }

        /**
         * 获取用户友好的错误消息
         */
        fun getUserFriendlyMessage(exception: Throwable): String {
            val appException =
                if (exception is AppException) {
                    exception
                } else {
                    convertToAppException(exception)
                }

            return appException.getUserFriendlyMessage()
        }

        /**
         * 判断异常是否可以重试
         */
        fun isRetryable(exception: Throwable): Boolean {
            val appException =
                if (exception is AppException) {
                    exception
                } else {
                    convertToAppException(exception)
                }

            return appException.isRetryable()
        }

        /**
         * 获取重试延迟时间
         */
        fun getRetryDelay(exception: Throwable): Long {
            val appException =
                if (exception is AppException) {
                    exception
                } else {
                    convertToAppException(exception)
                }

            return appException.getRetryDelay()
        }
    }
