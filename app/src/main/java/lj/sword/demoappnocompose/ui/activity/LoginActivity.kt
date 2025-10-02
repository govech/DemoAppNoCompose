package lj.sword.demoappnocompose.ui.activity

import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lj.sword.demoappnocompose.MainActivity
import lj.sword.demoappnocompose.base.BaseActivity
import lj.sword.demoappnocompose.base.UiState
import lj.sword.demoappnocompose.databinding.ActivityLoginBinding
import lj.sword.demoappnocompose.ext.onSingleClick
import lj.sword.demoappnocompose.ext.startActivityAndFinish
import lj.sword.demoappnocompose.ext.toast
import lj.sword.demoappnocompose.manager.Logger
import lj.sword.demoappnocompose.manager.TrackManager
import lj.sword.demoappnocompose.widget.LoadingDialog

/**
 * 登录页面
 * 演示框架的使用方法：
 * - BaseActivity 的使用
 * - ViewModel 的集成
 * - 网络请求和状态管理
 * - 自定义组件的使用
 * - Kotlin 扩展函数的使用
 * 
 * @author Sword
 * @since 1.0.0
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityLoginBinding = ActivityLoginBinding::inflate

    override val viewModel: LoginViewModel by viewModels()


    override fun initView() {
        // 初始化视图
        Logger.d("LoginActivity initialized")
    }

    override fun initData() {
        // 观察登录结果
        observeLoginResult()
    }

    override fun setListeners() {
        // 登录按钮点击（使用防抖扩展函数）
        binding.btnLogin.onSingleClick {
            performLogin()
        }

        // 忘记密码
        binding.tvForgotPassword.onSingleClick {
            toast("功能开发中...")
        }

        // 注册
        binding.tvRegister.onSingleClick {
            toast("功能开发中...")
        }

        // 微信登录
        binding.ivWechatLogin.onSingleClick {
            toast("微信登录功能开发中...")
        }

        // QQ登录
        binding.ivQQLogin.onSingleClick {
            toast("QQ登录功能开发中...")
        }
    }

    /**
     * 执行登录
     */
    private fun performLogin() {
        val username = binding.etUsername.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString()?.trim() ?: ""

        // 表单验证
        if (username.isEmpty()) {
            toast("请输入用户名")
            binding.etUsername.requestFocus()
            return
        }

        if (password.isEmpty()) {
            toast("请输入密码")
            binding.etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            toast("密码长度不能少于6位")
            binding.etPassword.requestFocus()
            return
        }

        // 埋点统计
        TrackManager.trackEvent("click_login", mapOf(
            "username" to username
        ))

        // 执行登录
        viewModel.login(username, password)
    }

    /**
     * 观察登录结果
     */
    private fun observeLoginResult() {
        lifecycleScope.launch {
            viewModel.loginResult.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // 显示加载中
                        LoadingDialog.show(this@LoginActivity, state.message)
                    }
                    is UiState.Success -> {
                        // 隐藏加载框
                        LoadingDialog.dismissLoading()
                        
                        // 登录成功
                        toast("登录成功！")
                        
                        val user = state.data
                        Logger.d("Login success: userId=${user.id}, username=${user.username}")
                        
                        // 埋点统计
                        TrackManager.trackEvent("login_success", mapOf(
                            "userId" to user.id,
                            "username" to user.username
                        ))
                        
                        // 跳转到主页（这里暂时关闭当前页面）
                        // startActivity<MainActivity>()
                        toast("登录功能演示完成！")
                        startActivityAndFinish<MainActivity>()
                        // 重置状态，允许再次登录测试
                        viewModel.resetLoginState()
                    }
                    is UiState.Error -> {
                        // 隐藏加载框
                        LoadingDialog.dismissLoading()

                        // 显示错误信息
                        toast(state.message)

                        Logger.e("Login failed: ${state.message}")

                        // 埋点统计
                        TrackManager.trackEvent("login_failed", mapOf(
                            "error" to state.message
                        ))
                    }
                    else -> {
                        // Empty 状态，不处理
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 页面浏览埋点
        TrackManager.trackPageView("LoginActivity")
    }

    override fun showLoading() {
        // 使用 LoadingDialog 替代默认实现
        LoadingDialog.show(this)
    }

    override fun hideLoading() {
        LoadingDialog.dismissLoading()
    }
}
