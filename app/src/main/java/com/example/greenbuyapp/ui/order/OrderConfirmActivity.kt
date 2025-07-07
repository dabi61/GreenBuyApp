package com.example.greenbuyapp.ui.order

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.cart.model.CartItem
import com.example.greenbuyapp.data.cart.model.CartShop
import com.example.greenbuyapp.data.user.model.UserMe
import com.example.greenbuyapp.databinding.ActivityOrderConfirmBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.cart.CartShopAdapter
import com.example.greenbuyapp.ui.cart.CartViewModel
import com.example.greenbuyapp.ui.profile.ProfileViewModel
import com.example.greenbuyapp.util.Result
import com.example.greenbuyapp.util.loadAvatar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.util.Locale


class OrderConfirmActivity : BaseActivity<ActivityOrderConfirmBinding>() {

    private var items: List<CartShop> = emptyList()
    override val viewModel: CartViewModel by viewModel()
    private val viewModelProfile: ProfileViewModel by viewModel()
    override val binding: ActivityOrderConfirmBinding by lazy {
        ActivityOrderConfirmBinding.inflate(layoutInflater)
    }

    private lateinit var orderAdapter: OrderShopAdapter
    private var fastShipping = true

    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        items = intent.getParcelableArrayListExtra<CartShop>(EXTRA_ITEMS) ?: emptyList()


        setupRecycler()
        updateUI(items)
        setupConfirmButton()
        initViews()
        observeViewModel()

    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }


    override fun observeViewModel() {
        super.observeViewModel()
        observeProfile()
    }

    private fun observeProfile() {
        lifecycleScope.launch {
            viewModelProfile.address.collect { address ->
                Log.d("Address", address.toString())
                if (address != null) {
                    for (item in address) {
                        if (item.is_default) {
                            binding.apply {
                                tvAddress.text = item.street
                                tvPhoneNumber.text = item.phone
                            }
                        }
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            viewModelProfile.userProfile.collect { result ->
                when (result) {
                    is Result.Success -> {
                        binding.tvName.text = getDisplayName(result.value)
                    }
                    is Result.Error -> {
                        showError("Lỗi khi tải thông tin user: ${result.error ?: "Lỗi không xác định"}")
                    }
                    is Result.NetworkError -> {
                        showError("Lỗi mạng, vui lòng kiểm tra kết nối internet")
                    }
                    is Result.Loading -> {
                        // Handle loading state if needed
                    }
                    null -> {
                        // Initial state, do nothing
                    }
                }
            }
        }
    }

    override fun initViews() {
        viewModelProfile.loadAddress()
        viewModelProfile.loadUserProfile()

        setupToolbar()
        setupSummaryTotal()
    }

    private fun setupSummaryTotal() {
        var totalAmount = 0.0
        var shippingAmount = 0.0
        for (item in items) {
            totalAmount += item.getTotalAmount()
            if (item.getTotalAmount() >= 500000) {
                shippingAmount += 0
            } else {
                shippingAmount += 45000
            }
        }
        binding.apply {
            tvNumberShipPeeDiscount.text = getFormattedTotalAmount(0.0)
            tvNumberTotalPrice.text = getFormattedTotalAmount(totalAmount)
            tvNumberShipPee.text = getFormattedTotalAmount(shippingAmount)
            tvNumberTotalPayment.text = getFormattedTotalAmount(totalAmount + shippingAmount)
            tvNumberTotalPayment2.text = getFormattedTotalAmount(totalAmount + shippingAmount)
        }

    }

    private fun setupRecycler() {
        orderAdapter = OrderShopAdapter(
            isFastShipping = true
        ) { isFast ->
            // Xử lý khi người dùng chọn shipping
            fastShipping = isFast
            // Có thể update UI hoặc lưu trạng thái
        }

        binding.recyclerViewCart.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(this@OrderConfirmActivity)
        }
    }


    private fun updateUI(cartShops: List<CartShop>) {
        val isEmpty = cartShops.isEmpty() || cartShops.all { !it.hasItems() }

        // Show/hide views based on cart state
//        binding.llEmptyCart.isVisible = isEmpty
        binding.llCartContent.isVisible = !isEmpty

        if (!isEmpty) {
            // Update cart items
            orderAdapter.submitList(cartShops)

            // Update summary
//            binding.tvTotalItems.text = "${viewModel.getTotalItemCount()} sản phẩm"
//            binding.tvSubtotal.text = viewModel.getFormattedTotalAmount()
//            binding.tvTotalAmount.text = viewModel.getFormattedTotalAmount()

            // Update button states
//            binding.btnClearCart.isEnabled = true
            binding.btnCheckout.isEnabled = true
        } else {
            // Update button states
//            binding.btnClearCart.isEnabled = false
            binding.btnCheckout.isEnabled = false
        }

        println("📊 UI updated - isEmpty: $isEmpty, total: ${viewModel.getFormattedTotalAmount()}")
    }

    private fun getDisplayName(user: UserMe): String {
        val fullName = buildString {
            if (!user.first_name.isNullOrBlank()) {
                append(user.first_name)
            }
            if (!user.last_name.isNullOrBlank()) {
                if (isNotEmpty()) append(" ")
                append(user.last_name)
            }
        }
        return if (fullName.isNotBlank()) fullName else user.username
    }

    private fun setupConfirmButton() {
        binding.btnCheckout.setOnClickListener {
            viewModel.placeOrderAndPayAll(
                items,
                shippingAddress = binding.tvAddress.text.toString(),
                phoneNumber = binding.tvPhoneNumber.text.toString(),
                recipientName = binding.tvName.text.toString(),
                deliveryNotes = "", // hoặc lấy từ EditText nếu có
                billingAddress = binding.tvAddress.text.toString()
            )
        }
    }

    fun getFormattedTotalAmount(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(price)
    }


    companion object {
        private const val EXTRA_ITEMS = "items"
        fun createIntent(context: Context, items: ArrayList<CartShop>): Intent {
            return Intent(context, OrderConfirmActivity::class.java).apply {
                putParcelableArrayListExtra(EXTRA_ITEMS, items)
            }
        }
    }
} 