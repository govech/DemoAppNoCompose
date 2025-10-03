package lj.sword.demoappnocompose.manager

/**
 * 埋点统计管理器
 * 统一管理应用的埋点上报
 *
 * @author Sword
 * @since 1.0.0
 */
object TrackManager {
    private var tracker: ITracker? = null

    /**
     * 是否启用埋点
     */
    var isEnabled = true

    /**
     * 初始化埋点
     */
    @JvmStatic
    fun init(tracker: ITracker) {
        this.tracker = tracker
    }

    /**
     * 页面浏览埋点
     */
    @JvmStatic
    fun trackPageView(
        pageName: String,
        params: Map<String, Any>? = null,
    ) {
        if (isEnabled) {
            tracker?.trackPageView(pageName, params)
        }
    }

    /**
     * 事件埋点
     */
    @JvmStatic
    fun trackEvent(
        eventName: String,
        params: Map<String, Any>? = null,
    ) {
        if (isEnabled) {
            tracker?.trackEvent(eventName, params)
        }
    }

    /**
     * 接口调用埋点
     */
    @JvmStatic
    fun trackApi(
        apiName: String,
        success: Boolean,
        duration: Long,
        params: Map<String, Any>? = null,
    ) {
        if (isEnabled) {
            tracker?.trackApi(apiName, success, duration, params)
        }
    }

    /**
     * 异常埋点
     */
    @JvmStatic
    fun trackException(
        throwable: Throwable,
        context: String? = null,
    ) {
        if (isEnabled) {
            tracker?.trackException(throwable, context)
        }
    }

    /**
     * 设置用户属性
     */
    @JvmStatic
    fun setUserProperty(
        key: String,
        value: Any,
    ) {
        if (isEnabled) {
            tracker?.setUserProperty(key, value)
        }
    }
}

/**
 * 埋点接口
 * 可以实现多个平台的埋点（友盟、神策等）
 */
interface ITracker {
    fun trackPageView(
        pageName: String,
        params: Map<String, Any>?,
    )

    fun trackEvent(
        eventName: String,
        params: Map<String, Any>?,
    )

    fun trackApi(
        apiName: String,
        success: Boolean,
        duration: Long,
        params: Map<String, Any>?,
    )

    fun trackException(
        throwable: Throwable,
        context: String?,
    )

    fun setUserProperty(
        key: String,
        value: Any,
    )
}

/**
 * 默认埋点实现（示例）
 * 实际项目中可以替换为具体的埋点 SDK
 */
class DefaultTracker : ITracker {
    override fun trackPageView(
        pageName: String,
        params: Map<String, Any>?,
    ) {
        Logger.d("[Track] PageView: $pageName, params: $params")
    }

    override fun trackEvent(
        eventName: String,
        params: Map<String, Any>?,
    ) {
        Logger.d("[Track] Event: $eventName, params: $params")
    }

    override fun trackApi(
        apiName: String,
        success: Boolean,
        duration: Long,
        params: Map<String, Any>?,
    ) {
        Logger.d("[Track] API: $apiName, success: $success, duration: ${duration}ms, params: $params")
    }

    override fun trackException(
        throwable: Throwable,
        context: String?,
    ) {
        Logger.e("[Track] Exception: ${throwable.message}, context: $context", throwable)
    }

    override fun setUserProperty(
        key: String,
        value: Any,
    ) {
        Logger.d("[Track] UserProperty: $key = $value")
    }
}
