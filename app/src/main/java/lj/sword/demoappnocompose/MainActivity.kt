package lj.sword.demoappnocompose

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cairong.permission.PermissionManager
import lj.sword.demoappnocompose.base.BaseActivity
import lj.sword.demoappnocompose.databinding.ActivityMainBinding
import lj.sword.demoappnocompose.ext.toast

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
        binding.btnRequestPermission.setOnClickListener {
            PermissionManager
                .with(this)
                .permissions(Manifest.permission.CAMERA)
                .onGranted { toast("相机权限已授权") }
                .onDenied { deniedPermissions, permanentlyDeniedPermissions ->
                    toast("相机权限被拒绝")
                }.request()
        }
    }
}
