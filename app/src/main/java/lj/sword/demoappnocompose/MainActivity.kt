package lj.sword.demoappnocompose

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import lj.sword.demoappnocompose.base.BaseActivity
import lj.sword.demoappnocompose.databinding.ActivityMainBinding
import lj.sword.demoappnocompose.ui.settings.LanguageSettingsActivity
import lj.sword.demoappnocompose.ui.settings.ThemeSettingsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun initView() {

    }



    override fun setListeners() {
        binding.btnLanguage.setOnClickListener {
            startActivity(Intent(this, LanguageSettingsActivity::class.java))
        }
        binding.btnTheme.setOnClickListener {
            startActivity(Intent(this, ThemeSettingsActivity::class.java))
        }
    }
}