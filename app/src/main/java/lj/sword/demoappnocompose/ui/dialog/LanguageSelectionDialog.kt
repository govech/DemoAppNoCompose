package lj.sword.demoappnocompose.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import lj.sword.demoappnocompose.data.model.LanguageConfig
import lj.sword.demoappnocompose.data.model.SupportedLanguage
import lj.sword.demoappnocompose.databinding.DialogLanguageSelectionBinding
import lj.sword.demoappnocompose.ui.adapter.LanguageAdapter

/**
 * 语言选择对话框
 * 
 * @author Sword
 * @since 1.0.0
 */
class LanguageSelectionDialog : DialogFragment() {

    private var _binding: DialogLanguageSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var languageAdapter: LanguageAdapter
    private var allLanguages: List<LanguageConfig> = emptyList()
    private var filteredLanguages: List<LanguageConfig> = emptyList()
    private var currentLanguage: LanguageConfig? = null
    private var onLanguageSelected: ((LanguageConfig) -> Unit)? = null

    companion object {
        private const val ARG_CURRENT_LANGUAGE = "current_language"

        /**
         * 创建语言选择对话框
         */
        fun newInstance(currentLanguage: LanguageConfig? = null): LanguageSelectionDialog {
            val dialog = LanguageSelectionDialog()
            val args = Bundle()
            args.putSerializable(ARG_CURRENT_LANGUAGE, currentLanguage)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Dialog)
        
        // 获取当前语言
        currentLanguage = arguments?.getSerializable(ARG_CURRENT_LANGUAGE) as? LanguageConfig
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLanguageSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews()
        setupRecyclerView()
        setupSearch()
        setupButtons()
        loadLanguages()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        // 设置对话框标题
        binding.tvTitle.text = "选择语言"
    }

    private fun setupRecyclerView() {
        languageAdapter = LanguageAdapter { languageConfig ->
            // 语言被选中
            languageAdapter.setSelectedLanguage(languageConfig)
        }
        
        binding.rvLanguages.apply {
            layoutManager = LinearLayoutManager(context)
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

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        
        binding.btnConfirm.setOnClickListener {
            val selectedLanguage = languageAdapter.getSelectedLanguage()
            if (selectedLanguage != null) {
                onLanguageSelected?.invoke(selectedLanguage)
                dismiss()
            }
        }
    }

    private fun loadLanguages() {
        // 获取所有支持的语言
        allLanguages = SupportedLanguage.getAllLanguages().map { language ->
            LanguageConfig(
                language = language,
                isSelected = language == currentLanguage?.language
            )
        }
        
        filteredLanguages = allLanguages
        languageAdapter.submitList(filteredLanguages)
        
        // 设置当前选中的语言
        currentLanguage?.let { language ->
            languageAdapter.setSelectedLanguage(language)
        }
    }

    private fun filterLanguages(query: String) {
        filteredLanguages = if (query.isBlank()) {
            allLanguages
        } else {
            allLanguages.filter { languageConfig ->
                languageConfig.getDisplayName().contains(query, ignoreCase = true) ||
                languageConfig.getNativeName().contains(query, ignoreCase = true) ||
                languageConfig.language.code.contains(query, ignoreCase = true)
            }
        }
        
        languageAdapter.submitList(filteredLanguages)
    }

    /**
     * 设置语言选择回调
     */
    fun setOnLanguageSelectedListener(listener: (LanguageConfig) -> Unit) {
        onLanguageSelected = listener
    }
}
