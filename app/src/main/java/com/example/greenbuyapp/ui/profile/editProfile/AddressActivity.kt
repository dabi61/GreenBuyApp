package com.example.greenbuyapp.ui.profile.editProfile

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.databinding.ActivityAddressBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.profile.address.AddressAdapter
import com.example.greenbuyapp.ui.profile.address.AddressViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddressActivity : BaseActivity<ActivityAddressBinding>() {

    override val viewModel: AddressViewModel by viewModel()

    override val binding: ActivityAddressBinding by lazy {
        ActivityAddressBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: AddressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        viewModel.loadUserInfor()
        initRecyclerView()
        observeData()

        viewModel.loadAddresses()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserInfor()
        viewModel.loadAddresses()
    }

    override fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.btnAddAddresss.setOnClickListener {
            val intent = Intent(this, AddressAddActivity::class.java)
            startActivity(intent)
        }

    }

    private fun initRecyclerView() {
        adapter = AddressAdapter()
        binding.recyclerViewAddress.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAddress.adapter = adapter
        // Xử lý khi giữ vào 1 item
        adapter.onItemLongClick = { addressId ->
            val intent = Intent(this, AddressUpdateActivity::class.java)
            intent.putExtra("address_id", addressId)
            startActivity(intent)
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            launch {
                viewModel.addresses.collectLatest { addressList ->
                    val fullName = "${viewModel.username.value.orEmpty()}".trim()
                    adapter.submitListWithName(addressList, fullName)
                }
            }
        }
    }
}
