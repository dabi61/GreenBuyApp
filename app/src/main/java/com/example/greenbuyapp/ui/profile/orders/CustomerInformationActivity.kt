package com.example.greenbuyapp.ui.profile.orders

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.greenbuyapp.data.user.model.UpdateUserProfileRequest
import com.example.greenbuyapp.data.user.model.UserMe
import com.example.greenbuyapp.databinding.ActivityCustomerInformationBinding
import com.example.greenbuyapp.ui.base.BaseActivity
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
    private var avatarUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun initViews() {
        setupToolbar()
        viewModel.getInfor()

        binding.tvBirthday.setOnClickListener { showDatePicker() }
        binding.btnSaveInfor.setOnClickListener { updateProfile() }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            // Quan sát thông tin người dùng
            launch {
                viewModel.userState.collectLatest { user ->
                    user?.let { bindUserToUI(it) }
                }
            }

            // Quan sát lỗi
            launch {
                viewModel.errorMessage.collectLatest { error ->
                    error?.let {
                        Toast.makeText(this@CustomerInformationActivity, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearErrorMessage()
                    }
                }
            }

            // Quan sát khi cập nhật thành công
            launch {
                viewModel.updateSuccess.collectLatest { success ->
                    if (success) {
                        Toast.makeText(this@CustomerInformationActivity, "Cập nhật thành công", Toast.LENGTH_SHORT).show()

                        viewModel.getInfor()
                        // Xoá trạng thái thành công sau khi xử lý
                        viewModel.clearUpdateSuccess()
                    }
                }
            }
        }
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
        avatarUrl = user.avatar

        // Chỉ load ảnh cũ nếu có
        Glide.with(this)
            .load(avatarUrl)
            .into(binding.imgAvatar)
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

        val request = UpdateUserProfileRequest(
            avatar = avatarUrl?.takeIf { it.isNotBlank() },
            first_name = ho,
            last_name = ten,
            phone_number = phone,
            birth_date = selectedBirthDate // Đã định dạng từ DatePicker
        )

        viewModel.updateProfile(request)
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
}
