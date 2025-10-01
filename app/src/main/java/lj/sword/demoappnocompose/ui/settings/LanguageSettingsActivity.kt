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

    override fun initView() {
        setupToolbar()
        setupRecyclerView()
        setupSearch()
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
            // 语言被选中
            languageViewModel.switchLanguage(languageConfig.language)
        }
        
        binding.rvLanguages.apply {
            layoutManager = LinearLayoutManager(this@LanguageSettingsActivity)
            adapter = languageAdapter
        }
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
                }
            }
        }

        lifecycleScope.launch {
            // 观察语言切换事件
            languageViewModel.languageSwitchEvent.collect { event ->
                event?.let { switchEvent ->
                    when (switchEvent) {
                        is LanguageViewModel.LanguageSwitchEvent.Success -> {
                            // 语言切换成功，显示提示并关闭页面
                            binding.root.toast(getString(lj.sword.demoappnocompose.R.string.language_confirm))
                            finish()
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
