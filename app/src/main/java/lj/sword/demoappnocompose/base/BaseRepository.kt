package lj.sword.demoappnocompose.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import lj.sword.demoappnocompose.data.model.ApiException
import lj.sword.demoappnocompose.data.model.BaseResponse

/**
 * Repository 基类
 * 提供统一的数据源切换逻辑、网络请求封装
 * 
 * @author Sword
 * @since 1.0.0
 */
abstract class BaseRepository {

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
        val response = block()
        
        if (response.isSuccess()) {
            val data = response.data
            if (data != null) {
                emit(data)
            } else {
                throw ApiException(
                    ApiException.CODE_PARSE_ERROR,
                    "数据为空"
                )
            }
        } else {
            throw ApiException(
                response.code,
                response.message.ifEmpty { "请求失败" }
            )
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
        val response = block()
        
        return if (response.isSuccess()) {
            response.data ?: throw ApiException(
                ApiException.CODE_PARSE_ERROR,
                "数据为空"
            )
        } else {
            throw ApiException(
                response.code,
                response.message.ifEmpty { "请求失败" }
            )
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
        emit(block())
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
