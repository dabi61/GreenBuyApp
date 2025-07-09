package com.example.greenbuyapp.ui.profile.editProfile.address

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.greenbuyapp.databinding.ActivityAddressAddBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddressAddActivity : BaseActivity<ActivityAddressAddBinding>() {

    override val binding: ActivityAddressAddBinding by lazy {
        ActivityAddressAddBinding.inflate(layoutInflater)
    }

    override val viewModel: AddressAddViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        initViews()
        setupViewModel()
    }

    override fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnSaveAddress.setOnClickListener {
            saveAddress()
        }
    }

    private fun saveAddress() {
        val street = binding.edtStreet.text.toString()
        val city = binding.edtCity.text.toString()
        val state = binding.edtState.text.toString()
        val zipcode = binding.edtZipcode.text.toString()
        val countryInput = binding.edtCountry.text.toString()
        val country = if (countryInput.isEmpty()) "Việt Nam" else countryInput
        val phone = binding.edtPhone.text.toString()

        if (!validateInput(street, city, state, zipcode, country, phone)) return

        viewModel.addAddress(
            street = street,
            city = city,
            state = state,
            zipcode = zipcode,
            country = country,
            phone = phone
        )
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

    private fun setupViewModel() {
        lifecycleScope.launch {
            // Observe success
            launch {
                viewModel.isSuccess.collect { isSuccess ->
                    if (isSuccess) {
                        Toast.makeText(
                            this@AddressAddActivity,
                            "Thêm địa chỉ thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }

            // Observe error
            launch {
                viewModel.errorMessage.collectLatest { error ->
                    error?.let {
                        Toast.makeText(
                            this@AddressAddActivity,
                            it,
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.clearErrorMessage()
                    }
                }
            }

            // Observe loading
            launch {
                viewModel.isLoading.collectLatest { isLoading ->
                    binding.btnSaveAddress.isEnabled = !isLoading
                }
            }
        }
    }
}