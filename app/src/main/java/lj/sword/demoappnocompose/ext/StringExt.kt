package lj.sword.demoappnocompose.ext

import lj.sword.demoappnocompose.utils.JsonUtil
import lj.sword.demoappnocompose.utils.RegexUtil

/**
 * 判断字符串是否为空或空白
 */
fun String?.isNullOrEmpty(): Boolean = this == null || this.isEmpty() || this.isBlank()

/**
 * 判断字符串非空
 */
fun String?.isNotNullOrEmpty(): Boolean = !this.isNullOrEmpty()

/**
 * 字符串为空时返回默认值
 */
fun String?.orDefault(default: String): String = if (this.isNullOrEmpty()) default else this!!

/**
 * 限制字符串长度
 */
fun String.limit(
    maxLength: Int,
    suffix: String = "...",
): String =
    if (this.length > maxLength) {
        this.substring(0, maxLength) + suffix
    } else {
        this
    }

/**
 * 验证手机号
 */
fun String.isMobile(): Boolean = RegexUtil.isMobile(this)

/**
 * 验证邮箱
 */
fun String.isEmail(): Boolean = RegexUtil.isEmail(this)

/**
 * 验证身份证号
 */
fun String.isIdCard(): Boolean = RegexUtil.isIdCard(this)

/**
 * 验证是否是 URL
 */
fun String.isUrl(): Boolean = RegexUtil.isUrl(this)

/**
 * 验证是否包含中文
 */
fun String.containsChinese(): Boolean = RegexUtil.containsChinese(this)

/**
 * JSON 字符串转对象
 */
inline fun <reified T> String.fromJson(): T? = JsonUtil.fromJson<T>(this)

/**
 * JSON 字符串转 List
 */
inline fun <reified T> String.fromJsonToList(): List<T>? = JsonUtil.fromJsonToList<T>(this)

/**
 * 判断是否是有效的 JSON
 */
fun String.isValidJson(): Boolean = JsonUtil.isValidJson(this)

/**
 * 格式化 JSON 字符串
 */
fun String.formatJson(): String = JsonUtil.formatJson(this)

/**
 * 移除所有空白字符
 */
fun String.removeAllWhitespace(): String = this.replace("\\s".toRegex(), "")

/**
 * 首字母大写
 */
fun String.capitalizeFirst(): String = if (this.isEmpty()) this else this[0].uppercase() + this.substring(1)

/**
 * 首字母小写
 */
fun String.decapitalizeFirst(): String = if (this.isEmpty()) this else this[0].lowercase() + this.substring(1)
