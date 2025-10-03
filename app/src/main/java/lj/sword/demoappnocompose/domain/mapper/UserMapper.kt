package lj.sword.demoappnocompose.domain.mapper

import lj.sword.demoappnocompose.data.model.LoginResponse
import lj.sword.demoappnocompose.data.model.UserEntity
import lj.sword.demoappnocompose.domain.model.User

/**
 * 用户数据转换器
 * 负责在不同层级的用户模型之间进行转换
 *
 * @author Sword
 * @since 1.0.0
 */

/**
 * LoginResponse 转换为 Domain User
 */
@Suppress("ktlint:standard:no-consecutive-comments")
fun LoginResponse.toDomainUser(): User =
    User(
        id = userId,
        username = username,
        avatar = avatar ?: "",
        email = email ?: "",
        token = token,
        createTime = System.currentTimeMillis(),
        lastLoginTime = System.currentTimeMillis(),
    )

/**
 * UserEntity 转换为 Domain User
 */
fun UserEntity.toDomainUser(): User =
    User(
        id = id,
        username = name,
        avatar = avatar ?: "",
        email = email ?: "",
        phone = phone ?: "",
        createTime = createTime,
    )

/**
 * Domain User 转换为 UserEntity
 */
fun User.toUserEntity(): UserEntity =
    UserEntity(
        id = id,
        name = username,
        avatar = avatar.takeIf { it.isNotBlank() },
        email = email.takeIf { it.isNotBlank() },
        phone = phone.takeIf { it.isNotBlank() },
        createTime = createTime,
    )

/**
 * Domain User 转换为 LoginResponse（用于缓存等场景）
 */
fun User.toLoginResponse(): LoginResponse =
    LoginResponse(
        userId = id,
        username = username,
        avatar = avatar,
        email = email,
        token = token,
    )
