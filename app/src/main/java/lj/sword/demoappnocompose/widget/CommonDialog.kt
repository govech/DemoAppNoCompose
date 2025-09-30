package lj.sword.demoappnocompose.widget

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import lj.sword.demoappnocompose.databinding.LayoutCommonDialogBinding

/**
 * 通用对话框
 * 支持单按钮、双按钮样式
 * 
 * @author Sword
 * @since 1.0.0
 */
class CommonDialog(context: Context) : Dialog(context, android.R.style.Theme_Dialog) {

    private val binding: LayoutCommonDialogBinding
    
    private var onConfirmListener: (() -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null

    init {
        binding = LayoutCommonDialogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // 设置按钮点击事件
        binding.btnConfirm.setOnClickListener {
            onConfirmListener?.invoke()
            dismiss()
        }
        
        binding.btnCancel.setOnClickListener {
            onCancelListener?.invoke()
            dismiss()
        }
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String): CommonDialog {
        binding.tvTitle.text = title
        return this
    }

    /**
     * 设置内容
     */
    fun setContent(content: String): CommonDialog {
        binding.tvContent.text = content
        return this
    }

    /**
     * 设置确定按钮文本
     */
    fun setConfirmText(text: String): CommonDialog {
        binding.btnConfirm.text = text
        return this
    }

    /**
     * 设置取消按钮文本
     */
    fun setCancelText(text: String): CommonDialog {
        binding.btnCancel.text = text
        return this
    }

    /**
     * 设置确定按钮监听器
     */
    fun setOnConfirmListener(listener: () -> Unit): CommonDialog {
        onConfirmListener = listener
        return this
    }

    /**
     * 设置取消按钮监听器
     */
    fun setOnCancelListener(listener: () -> Unit): CommonDialog {
        onCancelListener = listener
        return this
    }

    /**
     * 设置为单按钮模式
     */
    fun setSingleButton(): CommonDialog {
        binding.btnCancel.visibility = View.GONE
        binding.dividerVertical.visibility = View.GONE
        return this
    }

    companion object {
        /**
         * 创建 Builder
         */
        @JvmStatic
        fun builder(context: Context): Builder {
            return Builder(context)
        }
    }

    /**
     * 对话框 Builder
     */
    class Builder(private val context: Context) {
        private var title: String = "提示"
        private var content: String = ""
        private var confirmText: String = "确定"
        private var cancelText: String = "取消"
        private var isSingleButton: Boolean = false
        private var cancelable: Boolean = true
        private var onConfirmListener: (() -> Unit)? = null
        private var onCancelListener: (() -> Unit)? = null

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setContent(content: String): Builder {
            this.content = content
            return this
        }

        fun setConfirmText(text: String): Builder {
            this.confirmText = text
            return this
        }

        fun setCancelText(text: String): Builder {
            this.cancelText = text
            return this
        }

        fun setSingleButton(single: Boolean): Builder {
            this.isSingleButton = single
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        fun setOnConfirmListener(listener: () -> Unit): Builder {
            this.onConfirmListener = listener
            return this
        }

        fun setOnCancelListener(listener: () -> Unit): Builder {
            this.onCancelListener = listener
            return this
        }

        fun build(): CommonDialog {
            val dialog = CommonDialog(context)
            dialog.setTitle(title)
            dialog.setContent(content)
            dialog.setConfirmText(confirmText)
            dialog.setCancelText(cancelText)
            dialog.setCancelable(cancelable)
            
            if (isSingleButton) {
                dialog.setSingleButton()
            }
            
            onConfirmListener?.let { dialog.setOnConfirmListener(it) }
            onCancelListener?.let { dialog.setOnCancelListener(it) }
            
            return dialog
        }

        fun show(): CommonDialog {
            val dialog = build()
            dialog.show()
            return dialog
        }
    }
}
