# ADR-0004: 选择 Room 作为本地数据库

## 状态
已接受

## 背景
Android 应用需要本地数据存储功能，包括用户数据缓存、离线数据支持等。需要选择一个稳定、高效、易用的本地数据库解决方案。

## 决策
我们决定使用 Room 作为项目的本地数据库框架。

## 理由

### Room 优势
1. **官方支持**: Google 官方推荐的 Android 数据库解决方案
2. **编译时检查**: SQL 语句编译时验证，减少运行时错误
3. **类型安全**: 强类型 API，避免类型转换错误
4. **协程支持**: 原生支持 Kotlin 协程和 Flow
5. **迁移支持**: 完善的数据库版本迁移机制
6. **LiveData 集成**: 与 Android Architecture Components 无缝集成

### 与其他方案的比较
- **SQLite**: 原生 API，使用复杂，容易出错
- **Realm**: 第三方框架，学习成本高，迁移困难
- **ObjectBox**: 性能优秀，但生态不如 Room 完善
- **GreenDAO**: 老牌 ORM，但不支持协程

## 实现细节

### 数据库配置
```kotlin
@Database(
    entities = [UserEntity::class, PostEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()
}
```

### Entity 定义
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"])]
)
data class PostEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
```

### DAO 接口
```kotlin
@Dao
interface UserDao {
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithPosts(userId: String): UserWithPosts?
}
```

### 关系查询
```kotlin
data class UserWithPosts(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "user_id"
    )
    val posts: List<PostEntity>
)

@Dao
interface PostDao {
    @Transaction
    @Query("SELECT * FROM posts WHERE user_id = :userId")
    fun getPostsByUser(userId: String): Flow<List<PostEntity>>
}
```

### 类型转换器
```kotlin
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }
    
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}
```

### 数据库迁移
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE users ADD COLUMN avatar_url TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE posts_new (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                user_id TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """.trimIndent())
        
        database.execSQL("INSERT INTO posts_new SELECT * FROM posts")
        database.execSQL("DROP TABLE posts")
        database.execSQL("ALTER TABLE posts_new RENAME TO posts")
        database.execSQL("CREATE INDEX index_posts_user_id ON posts(user_id)")
    }
}

// 在 DatabaseModule 中使用
Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
    .build()
```

## 后果

### 正面影响
- 类型安全，减少运行时错误
- 编译时 SQL 验证，提高代码质量
- 协程和 Flow 支持，异步编程友好
- 完善的迁移机制，数据库升级安全
- 与 Android 架构组件无缝集成

### 负面影响
- 增加了依赖库的大小
- 编译时间略有增加
- 复杂查询的 SQL 编写仍需技巧

### 风险缓解
- 建立数据库设计规范
- 实现完善的数据迁移测试
- 提供 Room 使用的最佳实践文档
- 定期备份和恢复测试

## 性能优化

### 索引优化
```kotlin
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["created_at"]),
        Index(value = ["name", "email"])
    ]
)
data class UserEntity(...)
```

### 查询优化
```kotlin
@Dao
interface UserDao {
    // 分页查询
    @Query("SELECT * FROM users ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getUsersPaged(limit: Int, offset: Int): List<UserEntity>
    
    // 只查询需要的字段
    @Query("SELECT id, name FROM users")
    suspend fun getUserNamesOnly(): List<UserNameOnly>
    
    // 使用 Flow 进行响应式查询
    @Query("SELECT * FROM users WHERE name LIKE :query")
    fun searchUsers(query: String): Flow<List<UserEntity>>
}

data class UserNameOnly(
    val id: String,
    val name: String
)
```

### 事务处理
```kotlin
@Dao
interface UserDao {
    @Transaction
    suspend fun updateUserAndPosts(user: UserEntity, posts: List<PostEntity>) {
        updateUser(user)
        posts.forEach { insertPost(it) }
    }
}
```

## 相关决策
- ADR-0001: 选择 MVVM 架构模式
- ADR-0002: 选择 Hilt 作为依赖注入框架
- ADR-0005: 异常处理策略设计

---
*创建日期: 2024-01-01*  
*最后更新: 2024-01-01*  
*决策者: 开发团队*