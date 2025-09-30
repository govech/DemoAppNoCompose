package lj.sword.demoappnocompose.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch

/**
 * Activity 基类
 * 提供统一的 ViewBinding、ViewModel 绑定、状态栏管理、Loading 显示等功能
 * 
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型
 * 
 * @author Sword
 * @since 1.0.0
 */
abstract class BaseActivity<VB : ViewBinding, VM : BaseViewModel> : AppCompatActivity() {

    /**
     * ViewBinding 实例
     */
    protected lateinit var binding: VB

    /**
     * ViewModel 实例
     */
    protected abstract val viewModel: VM

    /**
     * 是否第一次加载
     */
    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化 ViewBinding
        binding = createBinding()
        setContentView(binding.root)

        // 初始化状态栏
        initStatusBar()

        // 初始化视图
        initView()

        // 初始化数据
        initData()

        // 观察 ViewModel 状态
        observeViewModel()

        // 设置监听器
        setListeners()
    }

    /**
     * 创建 ViewBinding（子类实现）
     */
    protected abstract fun createBinding(): VB

    /**
     * 初始化状态栏
     * 子类可重写以自定义状态栏样式
     */
    protected open fun initStatusBar() {
        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
        
        // 设置状态栏文字颜色为深色
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    /**
     * 初始化视图（子类实现）
     */
    protected abstract fun initView()

    /**
     * 初始化数据（子类可选实现）
     */
    protected open fun initData() {}

    /**
     * 设置监听器（子类可选实现）
     */
    protected open fun setListeners() {}

    /**
     * 观察 ViewModel 状态
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 观察 Loading 状态
                launch {
                    viewModel.loading.collect { isLoading ->
                        if (isLoading) {
                            showLoading()
                        } else {
                            hideLoading()
                        }
                    }
                }

                // 观察错误信息
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            showError(it)
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

    /**
     * 显示 Loading（子类可重写）
     */
    protected open fun showLoading() {
        // 将在实现 LoadingDialog 后完善
    }

    /**
     * 隐藏 Loading（子类可重写）
     */
    protected open fun hideLoading() {
        // 将在实现 LoadingDialog 后完善
    }

    /**
     * 显示错误信息（子类可重写）
     */
    protected open fun showError(message: String) {
        // 将在实现 Toast 扩展后完善
        // toast(message)
    }

    /**
     * 懒加载（首次可见时调用）
     */
    protected open fun lazyLoad() {}

    override fun onResume() {
        super.onResume()
        if (isFirstLoad) {
            lazyLoad()
            isFirstLoad = false
        }
    }
}
