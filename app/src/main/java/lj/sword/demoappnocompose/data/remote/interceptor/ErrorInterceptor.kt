package lj.sword.demoappnocompose.data.remote.interceptor

import lj.sword.demoappnocompose.data.model.ApiException
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * 错误处理拦截器
 * 统一处理网络请求错误
 *
 * @author Sword
 * @since 1.0.0
 */
class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        try {
            val response = chain.proceed(request)

            // 处理 HTTP 错误码
            if (!response.isSuccessful) {
                when (response.code) {
                    401 -> {
                        // Token 过期，触发重新登录逻辑
                        // 可以发送事件通知 UI 层
                        throw ApiException.tokenExpired()
                    }
                    403 -> {
                        throw ApiException(
                            ApiException.CODE_PERMISSION_DENIED,
                            ApiException.MSG_PERMISSION_DENIED,
                        )
                    }
                    in 500..599 -> {
                        throw ApiException.serverError()
                    }
                    else -> {
                        throw ApiException(
                            response.code,
                            response.message,
                        )
                    }
                }
            }

            return response
        } catch (e: SocketTimeoutException) {
            throw ApiException.timeoutError()
        } catch (e: IOException) {
            throw ApiException.networkError()
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw ApiException.unknownError()
        }
    }
}
