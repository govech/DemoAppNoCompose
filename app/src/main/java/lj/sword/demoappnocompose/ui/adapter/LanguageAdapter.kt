package lj.sword.demoappnocompose.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import lj.sword.demoappnocompose.data.model.LanguageConfig
import lj.sword.demoappnocompose.databinding.ItemLanguageBinding

/**
 * 语言选择适配器
 * 
 * @author Sword
 * @since 1.0.0
 */
class LanguageAdapter(
    private val onLanguageClick: (LanguageConfig) -> Unit
) : ListAdapter<LanguageConfig, LanguageAdapter.LanguageViewHolder>(LanguageDiffCallback()) {

    /** 当前选中的语言 */
    private var selectedLanguage: LanguageConfig? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val languageConfig = getItem(position)
        holder.bind(languageConfig, languageConfig == selectedLanguage)
    }

    /**
     * 设置选中的语言
     */
    fun setSelectedLanguage(languageConfig: LanguageConfig) {
        val oldSelected = selectedLanguage
        selectedLanguage = languageConfig
        
        // 更新UI
        oldSelected?.let { old ->
            val oldIndex = currentList.indexOf(old)
            if (oldIndex != -1) {
                notifyItemChanged(oldIndex)
            }
        }
        
        val newIndex = currentList.indexOf(languageConfig)
        if (newIndex != -1) {
            notifyItemChanged(newIndex)
        }
    }

    /**
     * 获取当前选中的语言
     */
    fun getSelectedLanguage(): LanguageConfig? = selectedLanguage

    inner class LanguageViewHolder(
        private val binding: ItemLanguageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val languageConfig = getItem(position)
                    onLanguageClick(languageConfig)
                }
            }
        }

        fun bind(languageConfig: LanguageConfig, isSelected: Boolean) {
            with(binding) {
                // 设置语言图标
                ivLanguageIcon.setImageResource(languageConfig.getIconRes())
                
                // 设置显示名称
                tvDisplayName.text = languageConfig.getDisplayName()
                
                // 设置原生名称
                tvNativeName.text = languageConfig.getNativeName()
                
                // 设置选中状态
                rbSelected.isChecked = isSelected
                
                // 设置选中状态的视觉反馈
                val alpha = if (isSelected) 1.0f else 0.6f
                root.alpha = alpha
            }
        }
    }

    /**
     * 语言配置差异回调
     */
    private class LanguageDiffCallback : DiffUtil.ItemCallback<LanguageConfig>() {
        override fun areItemsTheSame(oldItem: LanguageConfig, newItem: LanguageConfig): Boolean {
            return oldItem.language.code == newItem.language.code
        }

        override fun areContentsTheSame(oldItem: LanguageConfig, newItem: LanguageConfig): Boolean {
            return oldItem == newItem
        }
    }
}
