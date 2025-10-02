package lj.sword.demoappnocompose.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import lj.sword.demoappnocompose.base.exception.AppException
import lj.sword.demoappnocompose.base.exception.ExceptionHandler
import lj.sword.demoappnocompose.data.model.ApiException
import lj.sword.demoappnocompose.data.model.BaseResponse
import javax.inject.Inject

/**
 * Repository 基类
 * 提供统一的数据源切换逻辑、网络请求封装、异常处理
 * 
 * @author Sword
 * @since 1.0.0
 */
abstract class BaseRepository {

    @Inject
    lateinit var exceptionHandler: ExceptionHandler

    /**
     * 执行网络请求（Flow）
     * 自动处理响应结果和异常
     * 
     * @param block 网络请求代码块
     * @return Flow<T> 数据流
     */
    protected fun <T> executeRequest(
        block: suspend () -> BaseResponse<T>
    ): Flow<T> = flow {
        try {
            val response = block()
            
            if (response.isSuccess()) {
                val data = response.data
                if (data != null) {
                    emit(data)
                } else {
                    throw AppException.NetworkException.ParseError()
                }
            } else {
                // 根据响应码转换为对应的异常
                val exception = when (response.code) {
                    401 -> AppException.BusinessException.NotLoginError()
                    403 -> AppException.BusinessException.PermissionDeniedError()
                    404 -> AppException.NetworkException.HttpError(404, "资源不存在")
                    429 -> AppException.NetworkException.HttpError(429, "请求过于频繁")
                    in 500..599 -> AppException.NetworkException.ServerError(
                        response.message.ifEmpty { "服务器错误" },
                        code = response.code
                    )
                    else -> AppException.NetworkException.HttpError(
                        response.code,
                        response.message.ifEmpty { "请求失败" }
                    )
                }
                throw exception
            }
        } catch (e: Exception) {
            // 直接抛出异常，让上层处理
            throw e
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 执行网络请求（suspend）
     * 用于需要直接返回结果的场景
     * 
     * @param block 网络请求代码块
     * @return T 数据
     */
    protected suspend fun <T> executeRequestDirect(
        block: suspend () -> BaseResponse<T>
    ): T {
        return try {
            val response = block()
            
            if (response.isSuccess()) {
                response.data ?: throw AppException.NetworkException.ParseError()
            } else {
                val exception = when (response.code) {
                    401 -> AppException.BusinessException.NotLoginError()
                    403 -> AppException.BusinessException.PermissionDeniedError()
                    404 -> AppException.NetworkException.HttpError(404, "资源不存在")
                    429 -> AppException.NetworkException.HttpError(429, "请求过于频繁")
                    in 500..599 -> AppException.NetworkException.ServerError(
                        response.message.ifEmpty { "服务器错误" },
                        code = response.code
                    )
                    else -> AppException.NetworkException.HttpError(
                        response.code,
                        response.message.ifEmpty { "请求失败" }
                    )
                }
                throw exception
            }
        } catch (e: Exception) {
            // 直接抛出异常，让上层处理
            throw e
        }
    }

    /**
     * 执行本地数据库操作（Flow）
     * 
     * @param block 数据库操作代码块
     * @return Flow<T> 数据流
     */
    protected fun <T> executeLocal(
        block: suspend () -> T
    ): Flow<T> = flow {
        try {
            emit(block())
        } catch (e: Exception) {
            // 转换数据库异常
            val dbException = when (e) {
                is android.database.sqlite.SQLiteException -> {
                    when {
                        e.message?.contains("no such table") == true -> 
                            AppException.DatabaseException.QueryError(e)
                        e.message?.contains("database is locked") == true -> 
                            AppException.DatabaseException.ConnectionError(e)
                        e.message?.contains("UNIQUE constraint failed") == true -> 
                            AppException.DatabaseException.InsertError(e)
                        else -> AppException.DatabaseException.QueryError(e)
                    }
                }
                else -> AppException.DatabaseException.QueryError(e)
            }
            
            throw dbException
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 网络 + 缓存策略：先读取缓存，再请求网络
     * 
     * @param cacheBlock 读取缓存的代码块
     * @param networkBlock 网络请求代码块
     * @param saveBlock 保存缓存的代码块
     * @return Flow<T> 数据流
     */
    protected fun <T> executeCacheFirst(
        cacheBlock: suspend () -> T?,
        networkBlock: suspend () -> BaseResponse<T>,
        saveBlock: suspend (T) -> Unit
    ): Flow<T> = flow {
        // 先读取缓存
        val cache = cacheBlock()
        if (cache != null) {
            emit(cache)
        }

        // 再请求网络
        try {
            val response = networkBlock()
            if (response.isSuccess() && response.data != null) {
                // 保存到缓存
                saveBlock(response.data)
                emit(response.data)
            }
        } catch (e: Exception) {
            // 如果缓存为空且网络请求失败，则抛出异常
            if (cache == null) {
                throw e
            }
        }
    }.flowOn(Dispatchers.IO)
}
