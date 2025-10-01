package lj.sword.demoappnocompose.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch



/**
 * 无反射的 ViewBinding 扩展函数
 */
fun <VB : ViewBinding> AppCompatActivity.inflateBinding(
    inflater: (LayoutInflater) -> VB
): VB = inflater(layoutInflater)



/**
 * Activity 基类
 * 提供统一的 ViewBinding、ViewModel 绑定、状态栏管理、Loading 显示等功能
 *
 * @param VB ViewBinding 类型
 * @author Sword
 * @since 1.0.0
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    /** ViewBinding 实例 */
    protected lateinit var binding: VB

    /** 子类提供的 bindingInflater，例如 ActivityMainBinding::inflate */
    protected abstract val bindingInflater: (LayoutInflater) -> VB

    /** 可选的 ViewModel */
    protected open val viewModel: BaseViewModel? = null

    /** 是否第一次加载 */
    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用扩展函数自动创建 ViewBinding（无反射）
        binding = inflateBinding(bindingInflater)
        setContentView(binding.root)

        // 初始化流程
        initStatusBar()
        initView()
        initData()
        setListeners()
        observeViewModel()
    }

    /** 初始化状态栏 */
    protected open fun initStatusBar() {
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true // 替代已废弃的 SYSTEM_UI_FLAG
        }
    }

    /** 初始化视图 */
    protected abstract fun initView()

    /** 初始化数据（可选） */
    protected open fun initData() {}

    /** 设置监听器（可选） */
    protected open fun setListeners() {}

    /** 观察 ViewModel 状态 */
    private fun observeViewModel() {
        val vm = viewModel ?: return
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.loading.collect { if (it) showLoading() else hideLoading() } }
                launch { vm.error.collect { it?.let { msg -> showError(msg); vm.clearError() } } }
            }
        }
    }

    /** 显示 Loading */
    protected open fun showLoading() {}

    /** 隐藏 Loading */
    protected open fun hideLoading() {}

    /** 显示错误信息 */
    protected open fun showError(message: String) {}

    /** 懒加载 */
    protected open fun lazyLoad() {}

    override fun onResume() {
        super.onResume()
        if (isFirstLoad) {
            lazyLoad()
            isFirstLoad = false
        }
    }
}

