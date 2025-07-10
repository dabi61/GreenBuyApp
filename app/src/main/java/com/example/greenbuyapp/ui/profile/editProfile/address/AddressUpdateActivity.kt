package com.example.greenbuyapp.ui.profile.editProfile.address

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.greenbuyapp.data.user.model.AddressUpdateRequest
import com.example.greenbuyapp.databinding.ActivityAddressUpdateBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddressUpdateActivity : BaseActivity<ActivityAddressUpdateBinding>() {

    override val binding: ActivityAddressUpdateBinding by lazy {
        ActivityAddressUpdateBinding.inflate(layoutInflater)
    }

    override val viewModel: AddressUpdateViewModel by viewModel()

    private var addressId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        addressId = intent.getIntExtra("address_id", -1)
        if (addressId != -1) {
            viewModel.getAddressById(addressId)
        }

        binding.btnSaveAddress.setOnClickListener {
            updateAddress()
        }
        initViews()
        setViewModel()
    }
    override fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setViewModel() {
        lifecycleScope.launch {
            // Quan sát dữ liệu địa chỉ
            launch {
                viewModel.address.collect { address ->
                    address?.let {

                        binding.edtPhone.setText(it.phoneNumber)
                        binding.edtStreet.setText(it.street)
                        binding.edtCity.setText(it.city)
                        binding.edtState.setText(it.state)
                        binding.edtZipcode.setText(it.zipcode)
                        binding.edtCountry.setText(it.country)
                        binding.switchDefault.isChecked = it.isDefault == true
                    }
                }
            }

            // Quan sát khi cập nhật thành công
            launch {
                viewModel.updateSuccess.collect { success ->
                    if (success) {
                        showToast("✅ Cập nhật địa chỉ thành công")
                        finish()
                    }
                }
            }

            // Quan sát khi có lỗi
            launch {
                viewModel.errorMessage.collect { msg ->
                    msg?.let {
                        showToast("❌ $it")
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun updateAddress() {
        val street = binding.edtStreet.text.toString().trim()
        val city = binding.edtCity.text.toString().trim()
        val state = binding.edtState.text.toString().trim()
        val zipcode = binding.edtZipcode.text.toString().trim()
        val country = binding.edtCountry.text.toString().trim()
        val phone = binding.edtPhone.text.toString().trim()

        if (!validateInput(street, city, state, zipcode, country, phone)) return

        val request = AddressUpdateRequest(
            street = street,
            city = city,
            state = state,
            zipcode = zipcode,
            country = country,
            phoneNumber = phone,
            isDefault = binding.switchDefault.isChecked
        )
        viewModel.updateAddress(addressId, request)
    }

    private fun validateInput(
        street: String,
        city: String,
        state: String,
        zipcode: String,
        country: String,
        phone: String
    ): Boolean {
        if (street.isEmpty() || city.isEmpty() || state.isEmpty() ||
            zipcode.isEmpty() || country.isEmpty() || phone.isEmpty()
        ) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return false
        }

        // Kiểm tra số điện thoại chỉ chứa số
        if (!phone.matches(Regex("^\\d{8,15}$"))) {
            Toast.makeText(this, "Số điện thoại không hợp lệ (chỉ nhập số, tối thiểu 8 số)", Toast.LENGTH_SHORT).show()
            return false
        }

        // Kiểm tra country chỉ chứa chữ cái (kể cả tiếng Việt có dấu)
        if (!country.matches(Regex("^[\\p{L} ]+$"))) {
            Toast.makeText(this, "Quốc gia chỉ được nhập chữ cái", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
