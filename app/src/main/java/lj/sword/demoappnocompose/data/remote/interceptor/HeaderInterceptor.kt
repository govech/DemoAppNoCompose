package lj.sword.demoappnocompose.data.remote.interceptor

import lj.sword.demoappnocompose.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 请求头拦截器
 * 统一添加请求头信息（Token、语言、版本号等）
 * 
 * @author Sword
 * @since 1.0.0
 */
class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val requestBuilder = originalRequest.newBuilder()
            // 添加通用请求头
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("App-Version", BuildConfig.VERSION_NAME)
            .header("Platform", "Android")
        
        // 添加 Token（从 DataStore 读取，将在后续完善）
        // val token = getToken()
        // if (token.isNotEmpty()) {
        //     requestBuilder.header("Authorization", "Bearer $token")
        // }
        
        // 添加语言设置
        // val language = getLanguage()
        // requestBuilder.header("Accept-Language", language)
        
        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }

    // Token 获取方法将在实现 DataStore 后完善
    // private fun getToken(): String {
    //     return ""
    // }
}
