package com.example.greenbuyapp.ui.profile.editProfile.infomation

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.user.model.UpdateUserProfileRequest
import com.example.greenbuyapp.data.user.model.UpdateUserProfileResponse
import com.example.greenbuyapp.data.user.model.UserMe
import com.example.greenbuyapp.databinding.ActivityCustomerInformationBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.main.MainActivity
import com.example.greenbuyapp.ui.profile.editProfile.address.AddressActivity
import com.example.greenbuyapp.ui.shop.addProduct.AddProductUiState
import com.example.greenbuyapp.util.ImageTransform
import com.example.greenbuyapp.util.loadUrl
import com.example.greenbuyapp.util.loadAvatar
import com.example.greenbuyapp.util.clearImage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

class CustomerInformationActivity : BaseActivity<ActivityCustomerInformationBinding>() {

    override val viewModel: CustomerInformationViewModel by viewModel()
    override val binding: ActivityCustomerInformationBinding by lazy {
        ActivityCustomerInformationBinding.inflate(layoutInflater)
    }

    private var selectedBirthDate: String? = null
    private var avatarUrl: Uri? = null

    // Photo picker launcher
    private val photoPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        handleSelectedCoverImage(uri)
    }

    private fun openPhotoPicker() {
        photoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun setupCoverImagePicker() {
        binding.ivAvatar.setOnClickListener {
            openPhotoPicker()
        }
    }

    private fun handleSelectedCoverImage(uri: Uri?) {
        if (uri != null) {
            avatarUrl = uri

            // Hiển thị ảnh preview
            binding.ivAvatar.loadUrl(
                imageUrl = uri.toString(),
                placeholder = R.drawable.pic_item_product,
                error = R.drawable.pic_item_product,
                transform = ImageTransform.ROUNDED
            )

            println("📸 Avatar image selected: $uri")
            Toast.makeText(this, "✅ Đã chọn ảnh đại diện", Toast.LENGTH_SHORT).show()
        } else {
            println("❌ No avatar image selected")
            Toast.makeText(this, "❌ Không chọn ảnh nào", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun initViews() {
        setupToolbar()
        viewModel.getInfor()
        setupCoverImagePicker()
        binding.tvBirthday.setOnClickListener { showDatePicker() }
        binding.btnSaveInfor.setOnClickListener { updateProfile() }
    }

    override fun observeViewModel() {

        lifecycleScope.launch {
            viewModel.userState.collect { user ->
                user?.let { bindUserToUI(it) }
            }
        }


        lifecycleScope.launch {
            viewModel.updateInfomationState.collect { state ->
                when (state) {
                    is UpdateInfomationUiState.Idle -> {
                        binding.btnSaveInfor.isEnabled = true
                        binding.btnSaveInfor.text = "Lưu thông tin"
                    }
                    is UpdateInfomationUiState.Loading -> {
                        binding.btnSaveInfor.isEnabled = false
                        binding.btnSaveInfor.text = "Đang lưu..."
                        println("⏳ Creating product...")
                    }
                    is UpdateInfomationUiState.Success -> {
                        binding.btnSaveInfor.isEnabled = true
                        binding.btnSaveInfor.text = "Lưu thành công"
                        Toast.makeText(this@CustomerInformationActivity, "✅ Tạo sản phẩm thành công!", Toast.LENGTH_SHORT).show()
                        backToProfile()
                    }
                    is UpdateInfomationUiState.Error -> {
                        binding.btnSaveInfor.isEnabled = true
                        binding.btnSaveInfor.text = "Lưu thông tin"
                        Toast.makeText(this@CustomerInformationActivity, "❌ ${state.message}", Toast.LENGTH_LONG).show()
                        println("❌ Create product error: ${state.message}")
                    }
                }
            }
        }
    }

    private fun backToProfile() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fragment_index", 4)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun bindUserToUI(user: UserMe) {
        binding.edtHo.setText(user.first_name ?: "")
        binding.edtTen.setText(user.last_name ?: "")
        binding.edtPhone.setText(user.phone_number ?: "")
        binding.tvBirthday.text = user.birth_date?.take(10) ?: "Chọn ngày sinh"
        selectedBirthDate = user.birth_date

        // Load avatar hiện tại từ server với enhanced logging
        println("👤 CustomerInformationActivity: Loading user avatar")
        println("   Avatar path: ${user.avatar}")
        println("   Avatar null/empty: ${user.avatar.isNullOrEmpty()}")
        
        if (!user.avatar.isNullOrEmpty()) {
            // Clear cache trước khi load
            binding.ivAvatar.clearImage(R.drawable.avatar_blank)
            
            binding.ivAvatar.loadAvatar(
                avatarPath = user.avatar,
                placeholder = R.drawable.avatar_blank,
                error = R.drawable.avatar_blank,
                forceRefresh = true // ✅ Force refresh để đảm bảo ảnh mới nhất
            )
            println("✅ Avatar loading initiated for: ${user.avatar}")
        } else {
            println("⚠️ No avatar URL, using default avatar")
            binding.ivAvatar.setImageResource(R.drawable.avatar_blank)
        }
    }

    private fun updateProfile() {
        val ho = binding.edtHo.text.toString().trim()
        val ten = binding.edtTen.text.toString().trim()
        val phone = binding.edtPhone.text.toString().trim()

        if (ho.isEmpty()) {
            binding.edtHo.error = "Họ không được để trống"
            return
        }
        if (ten.isEmpty()) {
            binding.edtTen.error = "Tên không được để trống"
            return
        }
        if (phone.isEmpty()) {
            binding.edtPhone.error = "Số điện thoại không được để trống"
            return
        }
        if (selectedBirthDate.isNullOrEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày sinh", Toast.LENGTH_SHORT).show()
            return
        }

        // In log kiểm tra dữ liệu thật được gửi đi
        println("📤 Đang gửi PUT với dữ liệu:")
        println(" - first_name = $ho")
        println(" - last_name = $ten")
        println(" - phone_number = $phone")
        println(" - birth_date = $selectedBirthDate")
        println(" - avatar = $avatarUrl")

        viewModel.updateProfile(
            this,
            avatarUrl, // Có thể null nếu không chọn ảnh mới
            ho,
            ten,
            phone.toInt(),
            selectedBirthDate!!
        )

    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, y, m, d ->
            val date = String.format("%04d-%02d-%02d", y, m + 1, d)
            selectedBirthDate = "${date}T00:00:00.000Z"
            binding.tvBirthday.text = date
        }, year, month, day)

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    override fun onResume() {
        super.onResume()
        // Force refresh user info để đảm bảo avatar mới nhất được hiển thị
        println("🔄 CustomerInformationActivity: onResume - refreshing user info")
        viewModel.getInfor()
    }
}
