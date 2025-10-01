package lj.sword.demoappnocompose.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore 管理类
 * 统一管理应用配置信息的存储和读取
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class DataStoreManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        // Keys
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        
        // Default Values
        const val DEFAULT_LANGUAGE = "zh"
        const val THEME_MODE_SYSTEM = "system"
        const val THEME_MODE_LIGHT = "light"
        const val THEME_MODE_DARK = "dark"
    }

    // ==================== Token ====================
    
    /**
     * 保存 Token
     */
    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
        }
    }

    /**
     * 获取 Token（Flow）
     */
    fun getTokenFlow(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[KEY_TOKEN] ?: ""
        }
    }

    /**
     * 获取 Token（suspend）
     */
    suspend fun getToken(): String {
        var token = ""
        dataStore.data.collect { preferences ->
            token = preferences[KEY_TOKEN] ?: ""
        }
        return token
    }

    /**
     * 清除 Token
     */
    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_TOKEN)
        }
    }

    // ==================== User ID ====================
    
    /**
     * 保存用户 ID
     */
    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
        }
    }

    /**
     * 获取用户 ID（Flow）
     */
    fun getUserIdFlow(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[KEY_USER_ID] ?: ""
        }
    }

    // ==================== Language ====================
    
    /**
     * 保存语言设置
     */
    suspend fun saveLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }

    /**
     * 获取语言设置（Flow）
     */
    fun getLanguageFlow(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[KEY_LANGUAGE] ?: DEFAULT_LANGUAGE
        }
    }

    /**
     * 获取语言设置（suspend）
     */
    suspend fun getLanguage(): String {
        var language = DEFAULT_LANGUAGE
        dataStore.data.collect { preferences ->
            language = preferences[KEY_LANGUAGE] ?: DEFAULT_LANGUAGE
        }
        return language
    }

    /**
     * 清除语言设置
     */
    suspend fun clearLanguage() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_LANGUAGE)
        }
    }

    // ==================== Theme ====================
    
    /**
     * 保存主题模式
     */
    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    /**
     * 获取主题模式（Flow）
     */
    fun getThemeModeFlow(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[KEY_THEME_MODE] ?: THEME_MODE_SYSTEM
        }
    }

    // ==================== First Launch ====================
    
    /**
     * 保存首次启动标记
     */
    suspend fun setFirstLaunch(isFirst: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_FIRST_LAUNCH] = isFirst
        }
    }

    /**
     * 判断是否首次启动（Flow）
     */
    fun isFirstLaunchFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[KEY_IS_FIRST_LAUNCH] ?: true
        }
    }

    // ==================== Clear All ====================
    
    /**
     * 清除所有数据（退出登录时调用）
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * 清除用户相关数据（保留语言、主题等设置）
     */
    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_TOKEN)
            preferences.remove(KEY_USER_ID)
        }
    }
}
