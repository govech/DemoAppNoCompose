package lj.sword.demoappnocompose.network

import kotlinx.coroutines.runBlocking
import lj.sword.demoappnocompose.manager.Logger
import lj.sword.demoappnocompose.manager.LocaleManager
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络管理器
 * 处理网络请求和响应
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class NetworkManager @Inject constructor(
    private val apiService: ApiService,
    private val localeManager: LocaleManager
) {

    /**
     * 获取多语言内容
     */
    suspend fun getI18nContent(key: String): Result<I18nData> {
        return try {
            val languageCode = runBlocking { localeManager.getCurrentLanguageCode() }
            val response = apiService.getI18nContent(languageCode, key)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Unknown error"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Failed to get i18n content", e)
            Result.failure(e)
        }
    }

    /**
     * 获取用户信息
     */
    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val languageCode = runBlocking { localeManager.getCurrentLanguageCode() }
            val response = apiService.getUserProfile(languageCode)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Unknown error"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Failed to get user profile", e)
            Result.failure(e)
        }
    }

    /**
     * 获取系统配置
     */
    suspend fun getSystemConfig(): Result<SystemConfig> {
        return try {
            val languageCode = runBlocking { localeManager.getCurrentLanguageCode() }
            val response = apiService.getSystemConfig(languageCode)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Unknown error"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Logger.e("Failed to get system config", e)
            Result.failure(e)
        }
    }
}
