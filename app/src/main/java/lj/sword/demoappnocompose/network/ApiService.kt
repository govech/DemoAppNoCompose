package lj.sword.demoappnocompose.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * API服务接口
 * 定义网络请求接口
 * 
 * @author Sword
 * @since 1.0.0
 */
interface ApiService {

    /**
     * 获取多语言内容
     */
    @GET("i18n/content")
    suspend fun getI18nContent(
        @Header("Accept-Language") language: String? = null,
        @Query("key") key: String
    ): Response<I18nResponse>

    /**
     * 获取用户信息（多语言）
     */
    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Accept-Language") language: String? = null
    ): Response<UserProfileResponse>

    /**
     * 获取系统配置（多语言）
     */
    @GET("system/config")
    suspend fun getSystemConfig(
        @Header("Accept-Language") language: String? = null
    ): Response<SystemConfigResponse>
}

/**
 * 多语言响应数据类
 */
data class I18nResponse(
    val success: Boolean,
    val data: I18nData?,
    val message: String?
)

data class I18nData(
    val key: String,
    val value: String,
    val language: String
)

/**
 * 用户信息响应数据类
 */
data class UserProfileResponse(
    val success: Boolean,
    val data: UserProfile?,
    val message: String?
)

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val language: String,
    val timezone: String,
    val currency: String
)

/**
 * 系统配置响应数据类
 */
data class SystemConfigResponse(
    val success: Boolean,
    val data: SystemConfig?,
    val message: String?
)

data class SystemConfig(
    val appName: String,
    val version: String,
    val supportedLanguages: List<String>,
    val defaultLanguage: String,
    val features: Map<String, Boolean>
)
