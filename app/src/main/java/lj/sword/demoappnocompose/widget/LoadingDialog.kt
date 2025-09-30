package lj.sword.demoappnocompose.widget

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import lj.sword.demoappnocompose.databinding.LayoutLoadingDialogBinding

/**
 * 全局 Loading 对话框
 * 统一的加载提示样式
 * 
 * @author Sword
 * @since 1.0.0
 */
class LoadingDialog(context: Context) : Dialog(context, android.R.style.Theme_Translucent_NoTitleBar) {

    private val binding: LayoutLoadingDialogBinding

    init {
        binding = LayoutLoadingDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        
        // 设置窗口属性
        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            attributes = attributes?.apply {
                dimAmount = 0.2f
            }
        }
        
        // 设置为不可取消
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    /**
     * 设置加载提示文本
     */
    fun setMessage(message: String) {
        binding.tvMessage.text = message
    }

    companion object {
        private var instance: LoadingDialog? = null

        /**
         * 显示 Loading 对话框
         */
        @JvmStatic
        fun show(context: Context, message: String = "加载中..."): LoadingDialog {
            dismissLoading()
            instance = LoadingDialog(context).apply {
                setMessage(message)
                show()
            }
            return instance!!
        }

        /**
         * 隐藏 Loading 对话框
         */
        @JvmStatic
        fun dismissLoading() {
            instance?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
            instance = null
        }
    }
}
