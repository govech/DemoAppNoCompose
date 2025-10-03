package lj.sword.demoappnocompose.utils

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * 加密工具类
 * 提供 MD5、AES 等加密功能
 *
 * @author Sword
 * @since 1.0.0
 */
object EncryptUtil {
    /**
     * MD5 加密
     */
    @JvmStatic
    fun md5(input: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(input.toByteArray())
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * SHA-256 加密
     */
    @JvmStatic
    fun sha256(input: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray())
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * AES 加密
     * @param input 待加密字符串
     * @param key 密钥（16位）
     */
    @JvmStatic
    fun aesEncrypt(
        input: String,
        key: String,
    ): String? {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val keySpec = SecretKeySpec(key.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encrypted = cipher.doFinal(input.toByteArray())
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * AES 解密
     * @param encrypted 加密字符串
     * @param key 密钥（16位）
     */
    @JvmStatic
    fun aesDecrypt(
        encrypted: String,
        key: String,
    ): String? {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val keySpec = SecretKeySpec(key.toByteArray(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decoded = Base64.decode(encrypted, Base64.DEFAULT)
            val decrypted = cipher.doFinal(decoded)
            String(decrypted)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Base64 编码
     */
    @JvmStatic
    fun base64Encode(input: String): String {
        return Base64.encodeToString(input.toByteArray(), Base64.DEFAULT)
    }

    /**
     * Base64 解码
     */
    @JvmStatic
    fun base64Decode(input: String): String {
        return try {
            val decoded = Base64.decode(input, Base64.DEFAULT)
            String(decoded)
        } catch (e: Exception) {
            ""
        }
    }
}
