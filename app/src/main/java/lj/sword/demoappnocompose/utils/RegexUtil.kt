package lj.sword.demoappnocompose.utils

/**
 * 正则验证工具类
 * 提供常用的正则验证功能
 * 
 * @author Sword
 * @since 1.0.0
 */
object RegexUtil {

    /**
     * 手机号正则
     */
    private const val REGEX_MOBILE = "^1[3-9]\\d{9}$"

    /**
     * 邮箱正则
     */
    private const val REGEX_EMAIL = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$"

    /**
     * 身份证号正则（18位）
     */
    private const val REGEX_ID_CARD = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$"

    /**
     * 网址正则
     */
    private const val REGEX_URL = "^(http|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?$"

    /**
     * IP 地址正则
     */
    private const val REGEX_IP = "^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)$"

    /**
     * 中文正则
     */
    private const val REGEX_CHINESE = "^[\\u4e00-\\u9fa5]+$"

    /**
     * 验证手机号
     */
    @JvmStatic
    fun isMobile(mobile: String?): Boolean {
        return mobile?.matches(Regex(REGEX_MOBILE)) == true
    }

    /**
     * 验证邮箱
     */
    @JvmStatic
    fun isEmail(email: String?): Boolean {
        return email?.matches(Regex(REGEX_EMAIL)) == true
    }

    /**
     * 验证身份证号
     */
    @JvmStatic
    fun isIdCard(idCard: String?): Boolean {
        return idCard?.matches(Regex(REGEX_ID_CARD)) == true
    }

    /**
     * 验证网址
     */
    @JvmStatic
    fun isUrl(url: String?): Boolean {
        return url?.matches(Regex(REGEX_URL)) == true
    }

    /**
     * 验证 IP 地址
     */
    @JvmStatic
    fun isIp(ip: String?): Boolean {
        return ip?.matches(Regex(REGEX_IP)) == true
    }

    /**
     * 验证是否全是中文
     */
    @JvmStatic
    fun isChinese(text: String?): Boolean {
        return text?.matches(Regex(REGEX_CHINESE)) == true
    }

    /**
     * 验证是否包含中文
     */
    @JvmStatic
    fun containsChinese(text: String?): Boolean {
        return text?.any { it in '\u4e00'..'\u9fa5' } == true
    }

    /**
     * 验证密码强度
     * 至少包含大小写字母、数字、特殊字符中的三种，长度8-20位
     */
    @JvmStatic
    fun isStrongPassword(password: String?): Boolean {
        if (password == null || password.length !in 8..20) return false
        
        var typeCount = 0
        if (password.any { it.isLowerCase() }) typeCount++
        if (password.any { it.isUpperCase() }) typeCount++
        if (password.any { it.isDigit() }) typeCount++
        if (password.any { !it.isLetterOrDigit() }) typeCount++
        
        return typeCount >= 3
    }
}
