package lj.sword.demoappnocompose.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lj.sword.demoappnocompose.data.local.datastore.ThemeDataStore
import lj.sword.demoappnocompose.manager.ThemeManager
import javax.inject.Singleton

/**
 * 主题相关依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {
    
    @Provides
    @Singleton
    fun provideThemeDataStore(dataStore: DataStore<Preferences>): ThemeDataStore {
        return ThemeDataStore(dataStore)
    }
    
    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context,
        themeDataStore: ThemeDataStore
    ): ThemeManager {
        return ThemeManager(context, themeDataStore)
    }
}
