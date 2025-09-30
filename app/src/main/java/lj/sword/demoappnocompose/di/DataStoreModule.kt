package lj.sword.demoappnocompose.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DataStore 依赖注入模块
 * 提供 DataStore 相关依赖
 * 
 * @author Sword
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

    /**
     * 提供 DataStore 实例
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
