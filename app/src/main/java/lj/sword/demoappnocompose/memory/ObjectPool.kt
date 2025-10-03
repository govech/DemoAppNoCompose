package lj.sword.demoappnocompose.memory

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * 通用对象池
 * 减少对象创建和销毁的开销
 *
 * @author Sword
 * @since 1.0.0
 */
class ObjectPool<T>(
    private val factory: () -> T,
    private val reset: (T) -> Unit = {},
    private val maxSize: Int = 50,
) {
    private val pool = ConcurrentLinkedQueue<T>()
    private val currentSize = AtomicInteger(0)

    /**
     * 从对象池获取对象
     */
    fun acquire(): T {
        val obj = pool.poll()
        if (obj != null) {
            currentSize.decrementAndGet()
            return obj
        }
        return factory()
    }

    /**
     * 将对象归还到对象池
     */
    fun release(obj: T) {
        if (currentSize.get() < maxSize) {
            reset(obj)
            pool.offer(obj)
            currentSize.incrementAndGet()
        }
    }

    /**
     * 清空对象池
     */
    fun clear() {
        pool.clear()
        currentSize.set(0)
    }

    /**
     * 获取当前池中对象数量
     */
    fun size(): Int = currentSize.get()

    /**
     * 获取最大容量
     */
    fun maxSize(): Int = maxSize
}

/**
 * 对象池管理器
 * 管理多个对象池实例
 */
object ObjectPoolManager {
    private val pools = mutableMapOf<String, ObjectPool<*>>()

    /**
     * 注册对象池
     */
    fun <T> registerPool(
        name: String,
        pool: ObjectPool<T>,
    ) {
        pools[name] = pool
    }

    /**
     * 获取对象池
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getPool(name: String): ObjectPool<T>? {
        return pools[name] as? ObjectPool<T>
    }

    /**
     * 清空所有对象池
     */
    fun clearAll() {
        pools.values.forEach { it.clear() }
    }

    /**
     * 获取对象池统计信息
     */
    fun getStats(): Map<String, PoolStats> {
        return pools.mapValues { (_, pool) ->
            PoolStats(
                currentSize = pool.size(),
                maxSize = pool.maxSize(),
            )
        }
    }

    data class PoolStats(
        val currentSize: Int,
        val maxSize: Int,
    ) {
        val usagePercentage: Float
            get() = if (maxSize > 0) (currentSize.toFloat() / maxSize) * 100f else 0f
    }
}
