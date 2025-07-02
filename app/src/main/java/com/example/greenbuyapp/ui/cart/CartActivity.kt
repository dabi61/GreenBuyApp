package com.example.greenbuyapp.ui.cart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.databinding.ActivityCartBinding
import com.example.greenbuyapp.ui.base.BaseActivity
import com.example.greenbuyapp.ui.order.OrderConfirmActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CartActivity : BaseActivity<ActivityCartBinding>() {

    override val viewModel: CartViewModel by viewModel()
    override val binding: ActivityCartBinding by lazy { 
        ActivityCartBinding.inflate(layoutInflater) 
    }
    
    private lateinit var cartAdapter: CartShopAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun initViews() {
        setupToolbar()
        setupRecyclerView()
        setupButtons()
        
        println("🛒 CartActivity initialized")
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val selectedIds = mutableSetOf<Int>()

        cartAdapter = CartShopAdapter(
            onDeleteShop = { cartShop ->
                showDeleteShopConfirmDialog(cartShop.shopId, cartShop.shopName)
            },
            onUpdateQuantity = { attributeId, quantity ->
                viewModel.updateCartItemQuantity(attributeId, quantity)
            },
            onDeleteItem = { attributeId ->
                showDeleteItemConfirmDialog(attributeId)
            },
            selectedIds = selectedIds,
            onShopCheckedChanged = { shop, checked ->
                viewModel.toggleShopSelection(shop, checked)
            },
            onItemCheckedChanged = { attributeId, checked ->
                viewModel.toggleAttributeSelection(attributeId, checked)
            }
        )
        
        binding.recyclerViewCart.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(this@CartActivity)
        }
        
        println("✅ Cart RecyclerView setup completed")
    }

    private fun setupButtons() {
        // Clear cart button
//        binding.btnClearCart.setOnClickListener {
//            if (!viewModel.isCartEmpty()) {
//                showClearCartConfirmDialog()
//            }
//        }

        // Checkout button
        binding.btnCheckout.setOnClickListener {
            val selectedItems = viewModel.getSelectedCartItems()
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Chuyển sang OrderConfirmActivity và truyền danh sách selectedItems
            val intent = OrderConfirmActivity.createIntent(this, ArrayList(selectedItems))
            startActivity(intent)
        }

        // Shopping button (when cart is empty)
        binding.btnShopping.setOnClickListener {
            finish() // Quay về màn hình chính để mua sắm
        }
    }

    override fun observeViewModel() {
        observeCartData()
        observeLoadingState()
        observeErrorMessages()
        observeSuccessMessages()
        observeSelectedIds()
    }

    private fun observeCartData() {
        lifecycleScope.launch {
            viewModel.cartShops.collect { cartShops ->
                updateUI(cartShops)
                println("🛒 Cart data updated: ${cartShops.size} shops")
            }
        }
    }

    private fun observeLoadingState() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading
                println("⏳ Loading state: $isLoading")
            }
        }
    }

    private fun observeErrorMessages() {
        lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                if (!message.isNullOrEmpty()) {
                    Toast.makeText(this@CartActivity, message, Toast.LENGTH_LONG).show()
                    viewModel.clearErrorMessage()
                    println("❌ Error: $message")
                }
            }
        }
    }

    private fun observeSuccessMessages() {
        lifecycleScope.launch {
            viewModel.successMessage.collect { message ->
                if (!message.isNullOrEmpty()) {
                    Toast.makeText(this@CartActivity, message, Toast.LENGTH_SHORT).show()
                    viewModel.clearSuccessMessage()
                    println("✅ Success: $message")
                }
            }
        }
    }

    private fun observeSelectedIds() {
        lifecycleScope.launch {
            viewModel.selectedAttributeIds.collect { ids ->
                binding.btnCheckout.isEnabled = ids.isNotEmpty()

                // refresh order summary based on current selection
                binding.tvTotalItems.text = "${viewModel.getTotalItemCount()} sản phẩm"
                binding.tvSubtotal.text = viewModel.getFormattedTotalAmount()
                binding.tvTotalAmount.text = viewModel.getFormattedTotalAmount()
            }
        }
    }

    private fun updateUI(cartShops: List<com.example.greenbuyapp.data.cart.model.CartShop>) {
        val isEmpty = cartShops.isEmpty() || cartShops.all { !it.hasItems() }
        
        // Show/hide views based on cart state
        binding.llEmptyCart.isVisible = isEmpty
        binding.llCartContent.isVisible = !isEmpty
        binding.llBottomActions.isVisible = !isEmpty
        
        if (!isEmpty) {
            // Update cart items
            cartAdapter.submitList(cartShops)
            
            // Update summary
            binding.tvTotalItems.text = "${viewModel.getTotalItemCount()} sản phẩm"
            binding.tvSubtotal.text = viewModel.getFormattedTotalAmount()
            binding.tvTotalAmount.text = viewModel.getFormattedTotalAmount()
            
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

    private fun showDeleteShopConfirmDialog(shopId: Int, shopName: String) {
        AlertDialog.Builder(this)
            .setTitle("Xóa shop")
            .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm của shop \"$shopName\"?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteShopFromCart(shopId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showDeleteItemConfirmDialog(attributeId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Xóa sản phẩm")
            .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteCartItem(attributeId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showClearCartConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Xóa giỏ hàng")
            .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng?")
            .setPositiveButton("Xóa tất cả") { _, _ ->
                viewModel.clearAllCart()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    companion object {
        /**
         * Tạo intent để mở CartActivity
         */
        fun createIntent(context: Context): Intent {
            return Intent(context, CartActivity::class.java)
        }
    }
} 