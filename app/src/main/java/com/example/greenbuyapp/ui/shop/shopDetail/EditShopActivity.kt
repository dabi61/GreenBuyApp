package com.example.greenbuyapp.ui.shop.shopDetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.greenbuyapp.databinding.ActivityEditShopBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.util.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditShopActivity : BaseActivity<ActivityEditShopBinding>() {

    private val editShopViewModel: EditShopViewModel by viewModel()
    override val viewModel: ViewModel get() = editShopViewModel
    override val binding: ActivityEditShopBinding by lazy {
        ActivityEditShopBinding.inflate(layoutInflater)
    }

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        // Chọn ảnh
        binding.layoutPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }

        // Nút back
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Nút cập nhật
        binding.bottomButton.setOnClickListener {
            val name = binding.edtShopName.text.toString().trim()
            val phone = binding.edtPhone.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ tên và số điện thoại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh đại diện", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            editShopViewModel.updateShop(
                context = this,
                name = name,
                phoneNumber = phone,
                avatarUri = selectedImageUri
            )
        }

        observeUpdateResult()
    }

    private fun observeUpdateResult() {
        lifecycleScope.launchWhenStarted {
            editShopViewModel.updateResult.collect { result ->
                when (result) {
                    is Result.Success -> {
                        showSuccessLayout()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@EditShopActivity, "Lỗi: ${result}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun showSuccessLayout() {
        // Ẩn phần nhập liệu
        binding.cardAvatar.visibility = View.GONE
        binding.cardShopName.visibility = View.GONE
        binding.cardPhone.visibility = View.GONE
        binding.bottomButton.visibility = View.GONE

        // Hiện layout thành công
        binding.successLayout.visibility = View.VISIBLE

        // Đóng sau 1.5 giây
        Handler(Looper.getMainLooper()).postDelayed({
            setResult(RESULT_OK)
            finish()
        }, 1500)
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let {
                binding.ivSelectedAvatar.setImageURI(it)
            }
        }
    }

    companion object {
        private const val EXTRA_SHOP_ID = "extra_shop_id"

        fun createIntent(context: Context, shopId: Int): Intent {
            return Intent(context, EditShopActivity::class.java).apply {
                putExtra(EXTRA_SHOP_ID, shopId)
            }
        }
    }
}
