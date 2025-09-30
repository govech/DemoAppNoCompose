package lj.sword.demoappnocompose.ext

import lj.sword.demoappnocompose.utils.JsonUtil

/**
 * Any 扩展函数
 * 
 * @author Sword
 * @since 1.0.0
 */

/**
 * 对象转 JSON 字符串
 */
fun Any.toJson(): String {
    return JsonUtil.toJson(this)
}

/**
 * 对象转 JSON 字符串（格式化）
 */
fun Any.toPrettyJson(): String {
    return JsonUtil.toPrettyJson(this)
}
