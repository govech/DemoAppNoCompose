package lj.sword.demoappnocompose.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import lj.sword.demoappnocompose.data.model.SupportedLanguage
import lj.sword.demoappnocompose.manager.LanguageChangeBroadcastReceiver
import lj.sword.demoappnocompose.manager.LanguageChangeConstants

/**
 * Fragment基类
 * 提供语言切换支持
 * 
 * @author Sword
 * @since 1.0.0
 */
abstract class BaseFragment : Fragment() {
    
    private var languageChangeReceiver: BroadcastReceiver? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLanguageChangeReceiver()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterLanguageChangeReceiver()
    }
    
    /**
     * 注册语言变化广播接收器
     */
    private fun registerLanguageChangeReceiver() {
        languageChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LanguageChangeConstants.ACTION_LOCALE_CHANGED) {
                    val languageCode = intent.getStringExtra(LanguageChangeConstants.EXTRA_LANGUAGE_CODE)
                    languageCode?.let { code ->
                        val language = SupportedLanguage.fromCode(code)
                        if (language != null) {
                            onLocaleChanged(language)
                        }
                    }
                }
            }
        }
        
        val intentFilter = IntentFilter(LanguageChangeConstants.ACTION_LOCALE_CHANGED)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            languageChangeReceiver!!,
            intentFilter
        )
    }
    
    /**
     * 注销语言变化广播接收器
     */
    private fun unregisterLanguageChangeReceiver() {
        languageChangeReceiver?.let { receiver ->
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
        }
        languageChangeReceiver = null
    }
    
    /**
     * 语言变化回调
     * 子类可以重写此方法来处理语言变化
     */
    protected open fun onLocaleChanged(language: SupportedLanguage) {
        // 默认实现：刷新Fragment的UI
        refreshUI()
    }
    
    /**
     * 刷新UI
     * 子类可以重写此方法来实现具体的UI刷新逻辑
     */
    protected open fun refreshUI() {
        // 默认实现：什么都不做
        // 子类可以重写此方法来刷新具体的UI元素
    }
}