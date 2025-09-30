package lj.sword.demoappnocompose.constant

/**
 * 网络相关常量
 * 
 * @author Sword
 * @since 1.0.0
 */
object NetworkConstants {
    /**
     * 连接超时时间（秒）
     */
    const val CONNECT_TIMEOUT = 15L

    /**
     * 读取超时时间（秒）
     */
    const val READ_TIMEOUT = 15L

    /**
     * 写入超时时间（秒）
     */
    const val WRITE_TIMEOUT = 15L

    /**
     * 最大重试次数
     */
    const val MAX_RETRY_COUNT = 3
}
