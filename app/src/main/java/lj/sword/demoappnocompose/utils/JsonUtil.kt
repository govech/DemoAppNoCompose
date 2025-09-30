package lj.sword.demoappnocompose.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken

/**
 * JSON 工具类
 * 基于 Gson 提供 JSON 解析功能
 * 
 * @author Sword
 * @since 1.0.0
 */
object JsonUtil {

    @JvmStatic
    val gson: Gson by lazy {
        GsonBuilder()
            .serializeNulls()
            .create()
    }

    @JvmStatic
    val prettyGson: Gson by lazy {
        GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create()
    }

    /**
     * 对象转 JSON 字符串
     */
    @JvmStatic
    fun toJson(obj: Any?): String {
        return gson.toJson(obj)
    }

    /**
     * 对象转 JSON 字符串（格式化）
     */
    @JvmStatic
    fun toPrettyJson(obj: Any?): String {
        return prettyGson.toJson(obj)
    }

    /**
     * JSON 字符串转对象
     */
    @JvmStatic
    inline fun <reified T> fromJson(json: String): T? {
        return try {
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * JSON 字符串转对象（支持泛型）
     */
    @JvmStatic
    inline fun <reified T> fromJsonWithType(json: String): T? {
        return try {
            val type = object : TypeToken<T>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * JSON 字符串转 List
     */
    @JvmStatic
    inline fun <reified T> fromJsonToList(json: String): List<T>? {
        return try {
            val type = object : TypeToken<List<T>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * JSON 字符串转 Map
     */
    @JvmStatic
    fun fromJsonToMap(json: String): Map<String, Any>? {
        return try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 格式化 JSON 字符串
     */
    @JvmStatic
    fun formatJson(json: String): String {
        return try {
            val jsonElement = JsonParser.parseString(json)
            prettyGson.toJson(jsonElement)
        } catch (e: Exception) {
            json
        }
    }

    /**
     * 判断字符串是否是有效的 JSON
     */
    @JvmStatic
    fun isValidJson(json: String): Boolean {
        return try {
            JsonParser.parseString(json)
            true
        } catch (e: Exception) {
            false
        }
    }
}
