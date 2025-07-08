package com.example.greenbuyapp.ui.profile.editProfile

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityEditProfileBinding
import com.example.greenbuyapp.ui.base.BaseActivity

class EditProfileActivity :  BaseActivity<ActivityEditProfileBinding>() {
    override val binding: ActivityEditProfileBinding by lazy {
        ActivityEditProfileBinding.inflate(layoutInflater)
    }

    override val viewModel: ViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Gọi setContentView trước
        setContentView(binding.root)

        // Gọi super sau khi binding đã có
        super.onCreate(savedInstanceState)

        // Gọi lại hideNavigationBar tạm thời nếu cần
        hideNavigationBarTemporarily()
    }

    override fun initViews() {
        //Nút back
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        //chuyen sang form dia chi
        binding.llAddress.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            startActivity(intent)

        }
        binding.imgclickAddress.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            startActivity(intent)
        }
        binding.txtAddress.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            startActivity(intent)
        }

    }

}