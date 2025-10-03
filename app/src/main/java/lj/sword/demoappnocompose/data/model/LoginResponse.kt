package lj.sword.demoappnocompose.data.model

import com.google.gson.annotations.SerializedName

/**
 * 登录响应
 *
 * @property userId 用户ID
 * @property token 访问令牌
 * @property username 用户名
 * @property avatar 头像URL
 * @property email 邮箱
 *
 * @author Sword
 * @since 1.0.0
 */
data class LoginResponse(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("email")
    val email: String? = null,
)
