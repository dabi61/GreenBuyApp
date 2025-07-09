package com.example.greenbuyapp.ui.profile.editProfile

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.example.greenbuyapp.databinding.ActivityEditProfileBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.profile.editProfile.address.AddressActivity
import com.example.greenbuyapp.ui.profile.editProfile.infomation.CustomerInformationActivity

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
        //chuyen sang fomr thong tin ca nhan
        binding.txtPersonalInfo.setOnClickListener {
            val intent = Intent(this, CustomerInformationActivity::class.java)
            startActivity(intent)
        }
        binding.txtPersonalInfo2.setOnClickListener {
            val intent = Intent(this, CustomerInformationActivity::class.java)
            startActivity(intent)
        }
        binding.llInfor.setOnClickListener {
            val intent = Intent(this, CustomerInformationActivity::class.java)
            startActivity(intent)

        }

    }

}