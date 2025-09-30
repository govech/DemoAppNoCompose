package lj.sword.demoappnocompose.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户实体类（示例）
 * Room 数据库表
 * 
 * @author Sword
 * @since 1.0.0
 */
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val avatar: String?,
    val phone: String?,
    val email: String?,
    val createTime: Long = System.currentTimeMillis()
)
