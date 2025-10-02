package lj.sword.demoappnocompose.domain.model

/**
 * 用户业务模型
 * Domain层的用户实体，包含业务逻辑相关的属性和方法
 * 
 * @author Sword
 * @since 1.0.0
 */
data class User(
    val id: String,
    val username: String,
    val avatar: String = "",
    val email: String = "",
    val phone: String = "",
    val token: String = "",
    val createTime: Long = System.currentTimeMillis(),
    val lastLoginTime: Long = 0L
) {
    
    /**
     * 检查用户是否有效
     */
    fun isValid(): Boolean {
        return id.isNotBlank() && username.isNotBlank()
    }
    
    /**
     * 检查是否有头像
     */
    fun hasAvatar(): Boolean {
        return avatar.isNotBlank()
    }
    
    /**
     * 检查是否有邮箱
     */
    fun hasEmail(): Boolean {
        return email.isNotBlank()
    }
    
    /**
     * 检查是否有手机号
     */
    fun hasPhone(): Boolean {
        return phone.isNotBlank()
    }
    
    /**
     * 检查是否已认证（有邮箱或手机号）
     */
    fun isVerified(): Boolean {
        return hasEmail() || hasPhone()
    }
    
    /**
     * 获取显示名称（优先显示用户名）
     */
    fun getDisplayName(): String {
        return when {
            username.isNotBlank() -> username
            email.isNotBlank() -> email.substringBefore("@")
            phone.isNotBlank() -> phone
            else -> "用户$id"
        }
    }
    
    /**
     * 获取头像URL（如果没有则返回默认头像）
     */
    fun getAvatarUrl(): String {
        return if (avatar.isNotBlank()) {
            avatar
        } else {
            "https://via.placeholder.com/100x100?text=${getDisplayName().take(1)}"
        }
    }
    
    /**
     * 复制用户信息（用于更新）
     */
    fun copyWith(
        id: String = this.id,
        username: String = this.username,
        avatar: String = this.avatar,
        email: String = this.email,
        phone: String = this.phone,
        token: String = this.token,
        createTime: Long = this.createTime,
        lastLoginTime: Long = this.lastLoginTime
    ): User {
        return User(
            id = id,
            username = username,
            avatar = avatar,
            email = email,
            phone = phone,
            token = token,
            createTime = createTime,
            lastLoginTime = lastLoginTime
        )
    }
}