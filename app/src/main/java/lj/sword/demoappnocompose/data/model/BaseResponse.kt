package lj.sword.demoappnocompose.data.model

import com.google.gson.annotations.SerializedName

/**
 * 统一网络响应封装
 * 适配标准的后端响应格式
 * 
 * @param T 数据类型
 * @property code 响应码
 * @property message 响应消息
 * @property data 响应数据
 * 
 * @author Sword
 * @since 1.0.0
 */
data class BaseResponse<T>(
    @SerializedName("code")
    val code: Int = 0,
    
    @SerializedName("message", alternate = ["msg"])
    val message: String = "",
    
    @SerializedName("data")
    val data: T? = null
) {
    /**
     * 判断请求是否成功
     * 可根据实际后端约定修改成功码
     */
    fun isSuccess(): Boolean {
        return code == SUCCESS_CODE
    }

    companion object {
        /**
         * 成功响应码（根据实际后端约定修改）
         */
        const val SUCCESS_CODE = 200
        
        /**
         * Token 过期响应码（根据实际后端约定修改）
         */
        const val TOKEN_EXPIRED_CODE = 401
        
        /**
         * 权限不足响应码（根据实际后端约定修改）
         */
        const val PERMISSION_DENIED_CODE = 403
    }
}
