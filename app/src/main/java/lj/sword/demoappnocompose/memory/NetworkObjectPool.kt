package lj.sword.demoappnocompose.memory

import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络请求对象池
 * 复用网络请求相关的对象
 * 
 * @author Sword
 * @since 1.0.0
 */
@Singleton
class NetworkObjectPool @Inject constructor() {

    companion object {
        private const val BYTE_ARRAY_POOL_SIZE = 20
        private const val STRING_BUILDER_POOL_SIZE = 30
        private const val BYTE_ARRAY_OUTPUT_STREAM_POOL_SIZE = 15
        private const val DEFAULT_BUFFER_SIZE = 8192
    }

    // 字节数组对象池
    private val byteArrayPool = ObjectPool<ByteArray>(
        factory = { ByteArray(DEFAULT_BUFFER_SIZE) },
        reset = { /* ByteArray不需要重置 */ },
        maxSize = BYTE_ARRAY_POOL_SIZE
    )

    // StringBuilder对象池
    private val stringBuilderPool = ObjectPool<StringBuilder>(
        factory = { StringBuilder() },
        reset = { it.clear() },
        maxSize = STRING_BUILDER_POOL_SIZE
    )

    // ByteArrayOutputStream对象池
    private val byteArrayOutputStreamPool = ObjectPool<ByteArrayOutputStream>(
        factory = { ByteArrayOutputStream() },
        reset = { it.reset() },
        maxSize = BYTE_ARRAY_OUTPUT_STREAM_POOL_SIZE
    )

    init {
        // 注册到对象池管理器
        ObjectPoolManager.registerPool("byteArray", byteArrayPool)
        ObjectPoolManager.registerPool("stringBuilder", stringBuilderPool)
        ObjectPoolManager.registerPool("byteArrayOutputStream", byteArrayOutputStreamPool)
    }

    /**
     * 获取字节数组
     */
    fun acquireByteArray(): ByteArray {
        return byteArrayPool.acquire()
    }

    /**
     * 归还字节数组
     */
    fun releaseByteArray(byteArray: ByteArray) {
        byteArrayPool.release(byteArray)
    }

    /**
     * 获取StringBuilder
     */
    fun acquireStringBuilder(): StringBuilder {
        return stringBuilderPool.acquire()
    }

    /**
     * 归还StringBuilder
     */
    fun releaseStringBuilder(stringBuilder: StringBuilder) {
        stringBuilderPool.release(stringBuilder)
    }

    /**
     * 获取ByteArrayOutputStream
     */
    fun acquireByteArrayOutputStream(): ByteArrayOutputStream {
        return byteArrayOutputStreamPool.acquire()
    }

    /**
     * 归还ByteArrayOutputStream
     */
    fun releaseByteArrayOutputStream(outputStream: ByteArrayOutputStream) {
        byteArrayOutputStreamPool.release(outputStream)
    }

    /**
     * 使用字节数组执行操作
     */
    inline fun <T> useByteArray(block: (ByteArray) -> T): T {
        val byteArray = acquireByteArray()
        try {
            return block(byteArray)
        } finally {
            releaseByteArray(byteArray)
        }
    }

    /**
     * 使用StringBuilder执行操作
     */
    inline fun <T> useStringBuilder(block: (StringBuilder) -> T): T {
        val stringBuilder = acquireStringBuilder()
        try {
            return block(stringBuilder)
        } finally {
            releaseStringBuilder(stringBuilder)
        }
    }

    /**
     * 使用ByteArrayOutputStream执行操作
     */
    inline fun <T> useByteArrayOutputStream(block: (ByteArrayOutputStream) -> T): T {
        val outputStream = acquireByteArrayOutputStream()
        try {
            return block(outputStream)
        } finally {
            releaseByteArrayOutputStream(outputStream)
        }
    }

    /**
     * 清空所有对象池
     */
    fun clearAll() {
        byteArrayPool.clear()
        stringBuilderPool.clear()
        byteArrayOutputStreamPool.clear()
    }

    /**
     * 获取对象池统计信息
     */
    fun getPoolStats(): NetworkPoolStats {
        return NetworkPoolStats(
            byteArrayPoolSize = byteArrayPool.size(),
            stringBuilderPoolSize = stringBuilderPool.size(),
            byteArrayOutputStreamPoolSize = byteArrayOutputStreamPool.size(),
            byteArrayPoolMaxSize = byteArrayPool.maxSize(),
            stringBuilderPoolMaxSize = stringBuilderPool.maxSize(),
            byteArrayOutputStreamPoolMaxSize = byteArrayOutputStreamPool.maxSize()
        )
    }

    data class NetworkPoolStats(
        val byteArrayPoolSize: Int,
        val stringBuilderPoolSize: Int,
        val byteArrayOutputStreamPoolSize: Int,
        val byteArrayPoolMaxSize: Int,
        val stringBuilderPoolMaxSize: Int,
        val byteArrayOutputStreamPoolMaxSize: Int
    ) {
        fun getReport(): String {
            return buildString {
                appendLine("=== 网络对象池统计 ===")
                appendLine("字节数组池: $byteArrayPoolSize/$byteArrayPoolMaxSize")
                appendLine("StringBuilder池: $stringBuilderPoolSize/$stringBuilderPoolMaxSize")
                appendLine("ByteArrayOutputStream池: $byteArrayOutputStreamPoolSize/$byteArrayOutputStreamPoolMaxSize")
            }
        }
    }
}