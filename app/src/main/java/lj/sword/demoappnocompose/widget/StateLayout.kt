package lj.sword.demoappnocompose.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import lj.sword.demoappnocompose.R

/**
 * 状态布局
 * 支持 Loading、Error、Empty、Content 四种状态切换
 *
 * @author Sword
 * @since 1.0.0
 */
class StateLayout
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        /**
         * 状态枚举
         */
        enum class State {
            LOADING, // 加载中
            ERROR, // 错误
            EMPTY, // 空数据
            CONTENT, // 内容
        }

        private var contentView: View? = null
        private var loadingView: View? = null
        private var errorView: View? = null
        private var emptyView: View? = null

        private var currentState: State = State.CONTENT

        private var onRetryListener: (() -> Unit)? = null

        init {
            // 初始化默认的状态视图
            initDefaultViews()
        }

        /**
         * 初始化默认状态视图
         */
        private fun initDefaultViews() {
            val inflater = LayoutInflater.from(context)

            // 加载默认的 Loading、Error、Empty 视图
            loadingView = inflater.inflate(R.layout.layout_state_loading, this, false)
            errorView = inflater.inflate(R.layout.layout_state_error, this, false)
            emptyView = inflater.inflate(R.layout.layout_state_empty, this, false)

            // 设置重试按钮点击事件
            errorView?.findViewById<View>(R.id.btnRetry)?.setOnClickListener {
                onRetryListener?.invoke()
            }
        }

        override fun onFinishInflate() {
            super.onFinishInflate()

            // 获取第一个子视图作为 Content View
            if (childCount > 0) {
                contentView = getChildAt(0)
            }
        }

        /**
         * 显示加载中状态
         */
        fun showLoading() {
            showState(State.LOADING)
        }

        /**
         * 显示错误状态
         * @param message 错误信息
         */
        fun showError(message: String = "加载失败") {
            errorView?.findViewById<android.widget.TextView>(R.id.tvError)?.text = message
            showState(State.ERROR)
        }

        /**
         * 显示空数据状态
         * @param message 空数据提示
         */
        fun showEmpty(message: String = "暂无数据") {
            emptyView?.findViewById<android.widget.TextView>(R.id.tvEmpty)?.text = message
            showState(State.EMPTY)
        }

        /**
         * 显示内容
         */
        fun showContent() {
            showState(State.CONTENT)
        }

        /**
         * 切换状态
         */
        private fun showState(state: State) {
            if (currentState == state) return
            currentState = state

            // 移除之前的状态视图
            loadingView?.let { if (it.parent == this) removeView(it) }
            errorView?.let { if (it.parent == this) removeView(it) }
            emptyView?.let { if (it.parent == this) removeView(it) }

            // 根据状态显示对应的视图
            when (state) {
                State.LOADING -> {
                    contentView?.visibility = View.GONE
                    loadingView?.let {
                        if (it.parent == null) addView(it)
                        it.visibility = View.VISIBLE
                    }
                }
                State.ERROR -> {
                    contentView?.visibility = View.GONE
                    errorView?.let {
                        if (it.parent == null) addView(it)
                        it.visibility = View.VISIBLE
                    }
                }
                State.EMPTY -> {
                    contentView?.visibility = View.GONE
                    emptyView?.let {
                        if (it.parent == null) addView(it)
                        it.visibility = View.VISIBLE
                    }
                }
                State.CONTENT -> {
                    contentView?.visibility = View.VISIBLE
                }
            }
        }

        /**
         * 设置重试监听器
         */
        fun setOnRetryListener(listener: () -> Unit) {
            onRetryListener = listener
        }

        /**
         * 设置自定义 Loading 视图
         */
        fun setLoadingView(view: View) {
            loadingView = view
        }

        /**
         * 设置自定义 Error 视图
         */
        fun setErrorView(view: View) {
            errorView = view
        }

        /**
         * 设置自定义 Empty 视图
         */
        fun setEmptyView(view: View) {
            emptyView = view
        }

        /**
         * 获取当前状态
         */
        fun getCurrentState(): State = currentState
    }
