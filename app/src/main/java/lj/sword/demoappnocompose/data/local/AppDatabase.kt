package lj.sword.demoappnocompose.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import lj.sword.demoappnocompose.data.local.dao.UserDao
import lj.sword.demoappnocompose.data.model.UserEntity

/**
 * Room 数据库
 * 管理应用的本地数据库
 * 
 * @author Sword
 * @since 1.0.0
 */
@Database(
    entities = [
        UserEntity::class
        // 更多 Entity 可以在这里添加
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * 用户数据访问对象
     */
    abstract fun userDao(): UserDao
    
    // 更多 DAO 可以在这里添加
}
