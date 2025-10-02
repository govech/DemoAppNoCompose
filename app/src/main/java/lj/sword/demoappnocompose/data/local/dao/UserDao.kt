package lj.sword.demoappnocompose.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import lj.sword.demoappnocompose.data.model.UserEntity

/**
 * 用户数据访问对象（示例）
 * 
 * @author Sword
 * @since 1.0.0
 */
@Dao
interface UserDao {

    /**
     * 插入用户
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    /**
     * 插入用户（别名方法）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * 插入多个用户
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    /**
     * 更新用户
     */
    @Update
    suspend fun update(user: UserEntity)

    /**
     * 删除用户
     */
    @Delete
    suspend fun delete(user: UserEntity)

    /**
     * 根据 ID 查询用户
     */
    @Query("SELECT * FROM user WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    /**
     * 根据 ID 查询用户（Flow）
     */
    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    /**
     * 查询所有用户
     */
    @Query("SELECT * FROM user ORDER BY createTime DESC")
    suspend fun getAllUsers(): List<UserEntity>

    /**
     * 查询所有用户（Flow）
     */
    @Query("SELECT * FROM user ORDER BY createTime DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    /**
     * 根据用户ID删除用户
     */
    @Query("DELETE FROM user WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    /**
     * 删除所有用户
     */
    @Query("DELETE FROM user")
    suspend fun deleteAll()
}
