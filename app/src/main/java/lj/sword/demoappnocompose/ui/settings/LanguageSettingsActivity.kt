package lj.sword.demoappnocompose.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lj.sword.demoappnocompose.base.BaseActivity
import lj.sword.demoappnocompose.databinding.ActivityLanguageSettingsBinding
import lj.sword.demoappnocompose.ui.adapter.LanguageAdapter
import lj.sword.demoappnocompose.utils.getString
import lj.sword.demoappnocompose.utils.ext.toast

/**
 * 语言设置页面
 * 
 * @author Sword
 * @since 1.0.0
 */
@AndroidEntryPoint
class LanguageSettingsActivity : BaseActivity<ActivityLanguageSettingsBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityLanguageSettingsBinding = ActivityLanguageSettingsBinding::inflate

    private val languageViewModel: LanguageViewModel by viewModels()

    private lateinit var languageAdapter: LanguageAdapter
    
    /** 当前选中的语言（待确认） */
    private var selectedLanguageForConfirm: lj.sword.demoappnocompose.data.model.LanguageConfig? = null

    override fun initView() {
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupConfirmButton()
    }

    override fun initData() {
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        languageAdapter = LanguageAdapter { languageConfig ->
            // 语言被选中（但不立即切换）
            selectedLanguageForConfirm = languageConfig
            updateConfirmButtonState()
        }
        
        binding.rvLanguages.apply {
            layoutManager = LinearLayoutManager(this@LanguageSettingsActivity)
            adapter = languageAdapter
        }
    }

    private fun setupConfirmButton() {
        binding.btnConfirm.setOnClickListener {
            selectedLanguageForConfirm?.let { languageConfig ->
                // 点击确认按钮时才切换语言
                languageViewModel.switchLanguage(languageConfig.language)
            }
        }
    }

    /**
     * 更新确认按钮状态
     */
    private fun updateConfirmButtonState() {
        val currentLanguage = languageViewModel.currentLanguage.value
        val hasSelection = selectedLanguageForConfirm != null
        val isDifferentFromCurrent = selectedLanguageForConfirm != currentLanguage
        
        // 只有选中了不同于当前语言的选项时，确认按钮才可用
        binding.btnConfirm.isEnabled = hasSelection && isDifferentFromCurrent
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterLanguages(s.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // 观察加载状态
            languageViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            // 观察可用语言列表
            languageViewModel.availableLanguages.collect { languages ->
                languageAdapter.submitList(languages)
                
                // 显示/隐藏空状态
                binding.llEmptyState.visibility = if (languages.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            // 观察当前语言
            languageViewModel.currentLanguage.collect { currentLanguage ->
                currentLanguage?.let { language ->
                    languageAdapter.setSelectedLanguage(language)
                    // 如果还没有选择待确认的语言，默认选中当前语言
                    if (selectedLanguageForConfirm == null) {
                        selectedLanguageForConfirm = language
                    }
                    updateConfirmButtonState()
                }
            }
        }

        lifecycleScope.launch {
            // 观察语言切换事件
            languageViewModel.languageSwitchEvent.collect { event ->
                event?.let { switchEvent ->
                    when (switchEvent) {
                        is LanguageViewModel.LanguageSwitchEvent.Success -> {
                            // 语言切换成功，显示提示
                            binding.root.toast(getString(lj.sword.demoappnocompose.R.string.language_confirm))
                            // 延迟关闭页面，让广播有时间处理
                            binding.root.postDelayed({
                                finish()
                            }, 100)
                        }
                        is LanguageViewModel.LanguageSwitchEvent.Error -> {
                            // 语言切换失败，显示错误信息
                            binding.root.toast(switchEvent.message)
                        }
                    }
                    languageViewModel.clearLanguageSwitchEvent()
                }
            }
        }
    }

    private fun filterLanguages(query: String) {
        val allLanguages = languageViewModel.availableLanguages.value
        val filteredLanguages = if (query.isBlank()) {
            allLanguages
        } else {
            allLanguages.filter { languageConfig ->
                languageConfig.getDisplayName().contains(query, ignoreCase = true) ||
                languageConfig.getNativeName().contains(query, ignoreCase = true) ||
                languageConfig.language.code.contains(query, ignoreCase = true)
            }
        }
        
        languageAdapter.submitList(filteredLanguages)
        
        // 显示/隐藏空状态
        binding.llEmptyState.visibility = if (filteredLanguages.isEmpty()) View.VISIBLE else View.GONE
    }
}
