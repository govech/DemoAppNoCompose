package lj.sword.demoappnocompose.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lj.sword.demoappnocompose.data.model.AppTheme
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 主题数据存储管理器
 */
@Singleton
class ThemeDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_ID_KEY = stringPreferencesKey("theme_id")
        private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        private val FOLLOW_SYSTEM_KEY = booleanPreferencesKey("follow_system")
    }
    
    /**
     * 保存主题ID
     */
    suspend fun saveThemeId(themeId: String) {
        dataStore.edit { preferences ->
            preferences[THEME_ID_KEY] = themeId
        }
    }
    
    /**
     * 获取主题ID
     */
    fun getThemeId(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[THEME_ID_KEY] ?: AppTheme.DEFAULT.themeId
        }
    }
    
    /**
     * 保存暗黑模式设置
     */
    suspend fun saveDarkMode(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDarkMode
        }
    }
    
    /**
     * 获取暗黑模式设置
     */
    fun getDarkMode(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[IS_DARK_MODE_KEY] ?: false
        }
    }
    
    /**
     * 保存跟随系统设置
     */
    suspend fun saveFollowSystem(followSystem: Boolean) {
        dataStore.edit { preferences ->
            preferences[FOLLOW_SYSTEM_KEY] = followSystem
        }
    }
    
    /**
     * 获取跟随系统设置
     */
    fun getFollowSystem(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[FOLLOW_SYSTEM_KEY] ?: true
        }
    }
}
