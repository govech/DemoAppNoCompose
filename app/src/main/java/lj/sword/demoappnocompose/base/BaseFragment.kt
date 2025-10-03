package lj.sword.demoappnocompose.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch

/**
 * Fragment 基类
 * 提供统一的 ViewBinding、ViewModel 绑定、懒加载机制
 *
 * @param VB ViewBinding 类型
 * @param VM ViewModel 类型
 *
 * @author Sword
 * @since 1.0.0
 */
abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment() {
    /**
     * ViewBinding 实例
     */
    private var _binding: VB? = null
    protected val binding: VB get() = _binding!!

    /**
     * ViewModel 实例
     */
    protected abstract val viewModel: VM

    /**
     * 是否第一次加载
     */
    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = createBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化视图
        initView()

        // 观察 ViewModel 状态
        observeViewModel()

        // 设置监听器
        setListeners()
    }

    /**
     * 创建 ViewBinding（子类实现）
     */
    protected abstract fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): VB

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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
            initData()
            isFirstLoad = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
