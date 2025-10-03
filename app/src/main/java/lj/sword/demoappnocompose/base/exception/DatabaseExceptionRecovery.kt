package lj.sword.demoappnocompose.base.exception

import android.content.Context
import kotlinx.coroutines.delay
import lj.sword.demoappnocompose.manager.Logger
import lj.sword.demoappnocompose.utils.FileUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据库异常恢复策略
 * 处理数据库相关异常的恢复逻辑
 *
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class DatabaseExceptionRecovery
    @Inject
    constructor(
        private val context: Context,
    ) : ExceptionRecoveryStrategy {
        companion object {
            private const val RETRY_DELAY = 1000L
            private const val MIN_STORAGE_SPACE_MB = 100L // 最小存储空间要求（MB）
        }

        override fun canHandle(exception: Throwable): Boolean {
            return exception is AppException.DatabaseException
        }

        override suspend fun recover(
            exception: Throwable,
            retryAction: suspend () -> Unit,
        ): RecoveryResult {
            if (!canHandle(exception)) {
                return RecoveryResult.NonRetryableFailure("不支持的异常类型")
            }

            val dbException = exception as AppException.DatabaseException
            Logger.d("DatabaseExceptionRecovery", "处理数据库异常: ${dbException.javaClass.simpleName}")

            return when (dbException) {
                is AppException.DatabaseException.ConnectionError -> {
                    handleConnectionError(retryAction)
                }
                is AppException.DatabaseException.InsertError -> {
                    handleInsertError(retryAction)
                }
                is AppException.DatabaseException.QueryError -> {
                    handleQueryError(retryAction)
                }
                is AppException.DatabaseException.UpdateError -> {
                    handleUpdateError(retryAction)
                }
                is AppException.DatabaseException.DeleteError -> {
                    handleDeleteError(retryAction)
                }
                is AppException.DatabaseException.MigrationError -> {
                    handleMigrationError(dbException)
                }
            }
        }

        /**
         * 处理数据库连接错误
         */
        private suspend fun handleConnectionError(retryAction: suspend () -> Unit): RecoveryResult {
            return try {
                // 检查存储空间
                if (!hasEnoughStorage()) {
                    return RecoveryResult.RequiresUserIntervention(
                        message = "存储空间不足，请清理后重试",
                        actionType = UserActionType.CLEAN_STORAGE,
                    )
                }

                // 等待一段时间后重试
                delay(RETRY_DELAY)
                retryAction()
                RecoveryResult.Success
            } catch (e: Exception) {
                Logger.e("数据库连接重试失败", e, "DatabaseExceptionRecovery")
                RecoveryResult.RetryableFailure(
                    message = "数据库连接失败，正在重试...",
                    retryDelay = RETRY_DELAY * 2,
                )
            }
        }

        /**
         * 处理插入错误
         */
        private suspend fun handleInsertError(retryAction: suspend () -> Unit): RecoveryResult {
            return try {
                // 检查存储空间
                if (!hasEnoughStorage()) {
                    return RecoveryResult.RequiresUserIntervention(
                        message = "存储空间不足，无法保存数据",
                        actionType = UserActionType.CLEAN_STORAGE,
                    )
                }

                // 重试插入操作
                delay(RETRY_DELAY)
                retryAction()
                RecoveryResult.Success
            } catch (e: Exception) {
                Logger.e("数据插入重试失败", e, "DatabaseExceptionRecovery")
                RecoveryResult.NonRetryableFailure("数据保存失败，请稍后重试")
            }
        }

        /**
         * 处理查询错误
         */
        private suspend fun handleQueryError(retryAction: suspend () -> Unit): RecoveryResult {
            return try {
                // 查询错误通常可以重试
                delay(RETRY_DELAY)
                retryAction()
                RecoveryResult.Success
            } catch (e: Exception) {
                Logger.e("数据查询重试失败", e, "DatabaseExceptionRecovery")
                RecoveryResult.RetryableFailure(
                    message = "数据查询失败，正在重试...",
                    retryDelay = RETRY_DELAY,
                )
            }
        }

        /**
         * 处理更新错误
         */
        private suspend fun handleUpdateError(retryAction: suspend () -> Unit): RecoveryResult {
            return try {
                // 更新错误可能是并发冲突，重试一次
                delay(RETRY_DELAY)
                retryAction()
                RecoveryResult.Success
            } catch (e: Exception) {
                Logger.e("数据更新重试失败", e, "DatabaseExceptionRecovery")
                RecoveryResult.NonRetryableFailure("数据更新失败，请稍后重试")
            }
        }

        /**
         * 处理删除错误
         */
        private suspend fun handleDeleteError(retryAction: suspend () -> Unit): RecoveryResult {
            return try {
                // 删除错误可以重试
                delay(RETRY_DELAY)
                retryAction()
                RecoveryResult.Success
            } catch (e: Exception) {
                Logger.e("数据删除重试失败", e, "DatabaseExceptionRecovery")
                RecoveryResult.NonRetryableFailure("数据删除失败，请稍后重试")
            }
        }

        /**
         * 处理数据库迁移错误
         */
        private suspend fun handleMigrationError(exception: AppException.DatabaseException.MigrationError): RecoveryResult {
            Logger.e("数据库迁移失败", exception, "DatabaseExceptionRecovery")

            // 数据库迁移失败通常需要重新安装应用或联系客服
            return RecoveryResult.RequiresUserIntervention(
                message = "数据库升级失败，请联系客服或重新安装应用",
                actionType = UserActionType.CONTACT_SUPPORT,
            )
        }

        /**
         * 检查是否有足够的存储空间
         */
        private fun hasEnoughStorage(): Boolean {
            return try {
                val availableSpaceMB = FileUtil.getAvailableStorageSpace(context) / (1024 * 1024)
                availableSpaceMB >= MIN_STORAGE_SPACE_MB
            } catch (e: Exception) {
                Logger.e("检查存储空间失败", e, "DatabaseExceptionRecovery")
                true // 如果检查失败，假设有足够空间
            }
        }
    }
