package lj.sword.demoappnocompose.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import lj.sword.demoappnocompose.databinding.LayoutTitleBarBinding

/**
 * 通用标题栏
 * 支持标题、左右按钮配置
 * 
 * @author Sword
 * @since 1.0.0
 */
class TitleBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutTitleBarBinding

    init {
        binding = LayoutTitleBarBinding.inflate(LayoutInflater.from(context), this, true)
        
        // 默认返回按钮点击事件
        binding.ivBack.setOnClickListener {
            if (context is android.app.Activity) {
                context.finish()
            }
        }
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    /**
     * 设置标题
     */
    fun setTitle(titleResId: Int) {
        binding.tvTitle.setText(titleResId)
    }

    /**
     * 设置返回按钮是否可见
     */
    fun setBackVisible(visible: Boolean) {
        binding.ivBack.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * 设置返回按钮图标
     */
    fun setBackIcon(iconResId: Int) {
        binding.ivBack.setImageResource(iconResId)
    }

    /**
     * 设置返回按钮点击监听
     */
    fun setOnBackClickListener(listener: OnClickListener) {
        binding.ivBack.setOnClickListener(listener)
    }

    /**
     * 设置右侧文字按钮
     */
    fun setRightText(text: String) {
        binding.tvRight.text = text
        binding.tvRight.visibility = View.VISIBLE
        binding.ivRight.visibility = View.GONE
    }

    /**
     * 设置右侧文字按钮
     */
    fun setRightText(textResId: Int) {
        binding.tvRight.setText(textResId)
        binding.tvRight.visibility = View.VISIBLE
        binding.ivRight.visibility = View.GONE
    }

    /**
     * 设置右侧文字按钮点击监听
     */
    fun setOnRightTextClickListener(listener: OnClickListener) {
        binding.tvRight.setOnClickListener(listener)
    }

    /**
     * 设置右侧图标按钮
     */
    fun setRightIcon(iconResId: Int) {
        binding.ivRight.setImageResource(iconResId)
        binding.ivRight.visibility = View.VISIBLE
        binding.tvRight.visibility = View.GONE
    }

    /**
     * 设置右侧图标按钮点击监听
     */
    fun setOnRightIconClickListener(listener: OnClickListener) {
        binding.ivRight.setOnClickListener(listener)
    }

    /**
     * 设置背景颜色
     */
    fun setTitleBarBackgroundColor(color: Int) {
        binding.rootLayout.setBackgroundColor(color)
    }

    /**
     * 设置标题颜色
     */
    fun setTitleColor(color: Int) {
        binding.tvTitle.setTextColor(color)
    }
}
