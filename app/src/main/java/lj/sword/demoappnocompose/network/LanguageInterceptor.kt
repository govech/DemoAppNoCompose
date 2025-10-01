package lj.sword.demoappnocompose.network

import kotlinx.coroutines.runBlocking
import lj.sword.demoappnocompose.data.model.SupportedLanguage
import lj.sword.demoappnocompose.manager.LocaleManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 语言拦截器
 * 自动为网络请求添加语言头信息
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class LanguageInterceptor @Inject constructor(
    private val localeManager: LocaleManager
) : Interceptor {

    companion object {
        private const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
        private const val HEADER_CONTENT_LANGUAGE = "Content-Language"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 获取当前语言代码
        val languageCode = runBlocking { localeManager.getCurrentLanguageCode() }
        
        // 构建新的请求，添加语言头
        val newRequest = originalRequest.newBuilder()
            .addHeader(HEADER_ACCEPT_LANGUAGE, languageCode)
            .addHeader(HEADER_CONTENT_LANGUAGE, languageCode)
            .build()

        return chain.proceed(newRequest)
    }
}
