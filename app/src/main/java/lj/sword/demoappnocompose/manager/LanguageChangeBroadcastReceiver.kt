package lj.sword.demoappnocompose.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import lj.sword.demoappnocompose.data.model.SupportedLanguage

/**
 * 语言切换广播常量
 */
object LanguageChangeConstants {
    const val ACTION_LOCALE_CHANGED = "lj.sword.demoappnocompose.ACTION_LOCALE_CHANGED"
    const val EXTRA_LANGUAGE_CODE = "language_code"
}

/**
 * 语言切换广播接收器
 * 用于通知所有 Activity 语言已发生变化
 */
class LanguageChangeBroadcastReceiver(
    private val onLanguageChanged: (SupportedLanguage) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == LanguageChangeConstants.ACTION_LOCALE_CHANGED) {
            val languageCode = intent.getStringExtra(LanguageChangeConstants.EXTRA_LANGUAGE_CODE)
            languageCode?.let { code ->
                val language = SupportedLanguage.fromCode(code) ?: SupportedLanguage.getDefault()
                onLanguageChanged(language)
            }
        }
    }

    companion object {
        /**
         * 发送语言变化广播
         */
        fun sendLanguageChangedBroadcast(context: Context, languageCode: String) {
            val intent = Intent(LanguageChangeConstants.ACTION_LOCALE_CHANGED).apply {
                putExtra(LanguageChangeConstants.EXTRA_LANGUAGE_CODE, languageCode)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}
