package lj.sword.demoappnocompose.data.model

/**
 * 登录请求
 * 
 * @property username 用户名
 * @property password 密码
 * 
 * @author Sword
 * @since 1.0.0
 */
data class LoginRequest(
    val username: String,
    val password: String
)
