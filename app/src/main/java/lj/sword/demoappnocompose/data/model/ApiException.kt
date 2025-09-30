package lj.sword.demoappnocompose.data.model

/**
 * API 异常类
 * 封装网络请求过程中的各种异常
 * 
 * @property code 错误码
 * @property msg 错误信息
 * 
 * @author Sword
 * @since 1.0.0
 */
class ApiException(
    val code: Int,
    val msg: String
) : Exception(msg) {

    companion object {
        // 网络异常
        const val CODE_NETWORK_ERROR = -1000
        const val MSG_NETWORK_ERROR = "网络连接失败，请检查网络设置"

        // 服务器异常
        const val CODE_SERVER_ERROR = -1001
        const val MSG_SERVER_ERROR = "服务器异常，请稍后重试"

        // 解析异常
        const val CODE_PARSE_ERROR = -1002
        const val MSG_PARSE_ERROR = "数据解析失败"

        // 超时异常
        const val CODE_TIMEOUT_ERROR = -1003
        const val MSG_TIMEOUT_ERROR = "网络请求超时"

        // 未知异常
        const val CODE_UNKNOWN_ERROR = -1004
        const val MSG_UNKNOWN_ERROR = "未知错误"

        // Token 过期
        const val CODE_TOKEN_EXPIRED = 401
        const val MSG_TOKEN_EXPIRED = "登录信息已过期，请重新登录"

        // 权限不足
        const val CODE_PERMISSION_DENIED = 403
        const val MSG_PERMISSION_DENIED = "权限不足"

        /**
         * 创建网络异常
         */
        fun networkError(): ApiException {
            return ApiException(CODE_NETWORK_ERROR, MSG_NETWORK_ERROR)
        }

        /**
         * 创建服务器异常
         */
        fun serverError(): ApiException {
            return ApiException(CODE_SERVER_ERROR, MSG_SERVER_ERROR)
        }

        /**
         * 创建解析异常
         */
        fun parseError(): ApiException {
            return ApiException(CODE_PARSE_ERROR, MSG_PARSE_ERROR)
        }

        /**
         * 创建超时异常
         */
        fun timeoutError(): ApiException {
            return ApiException(CODE_TIMEOUT_ERROR, MSG_TIMEOUT_ERROR)
        }

        /**
         * 创建未知异常
         */
        fun unknownError(): ApiException {
            return ApiException(CODE_UNKNOWN_ERROR, MSG_UNKNOWN_ERROR)
        }

        /**
         * 创建 Token 过期异常
         */
        fun tokenExpired(): ApiException {
            return ApiException(CODE_TOKEN_EXPIRED, MSG_TOKEN_EXPIRED)
        }
    }
}
