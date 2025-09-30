package lj.sword.demoappnocompose.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lj.sword.demoappnocompose.data.local.AppDatabase
import lj.sword.demoappnocompose.data.local.dao.UserDao
import javax.inject.Singleton

/**
 * 数据库依赖注入模块
 * 提供 Room 数据库相关依赖
 * 
 * @author Sword
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 提供 Room 数据库实例
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration() // 简单情况下可以使用，生产环境建议实现 Migration
            .build()
    }

    /**
     * 提供 UserDao 实例
     */
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}
