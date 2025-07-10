package com.example.greenbuyapp.ui.shop

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.FragmentShopBinding
import com.example.greenbuyapp.ui.base.BaseFragment
import com.example.greenbuyapp.ui.home.BannerAdapter
import com.example.greenbuyapp.ui.shop.dashboard.ShopDashboardDetailActivity
import com.example.greenbuyapp.ui.shop.productManagement.ProductManagementActivity
import com.example.greenbuyapp.util.ImageTransform
import com.example.greenbuyapp.util.loadAvatar
import com.example.greenbuyapp.util.loadUrl
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import com.example.greenbuyapp.ui.admin.approve.product.ApproveProductActivity
import com.example.greenbuyapp.ui.shop.myShopDetail.MyShopDetailActivity

/**
 * Fragment hiển thị màn hình shop
 */
class ShopFragment : BaseFragment<FragmentShopBinding, ShopViewModel>() {

    override val viewModel: ShopViewModel by viewModel()

    private lateinit var bannerAdapter: BannerAdapter
    // ✅ Sử dụng Handler thay vì Timer để tránh ANR
    private val bannerHandler = Handler(Looper.getMainLooper())
    private var bannerRunnable: Runnable? = null
    private var isUserScrolling = false

    // ✅ Photo picker launcher
    private val photoPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        handleSelectedImage(uri)
    }
    
    // ✅ Lưu Uri của ảnh được chọn
    private var selectedAvatarUri: Uri? = null

    override fun getLayoutResourceId(): Int = R.layout.fragment_shop

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentShopBinding {
        return FragmentShopBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        // ✅ Null check cho activity
        if (!isAdded || activity == null) return
        
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        runCatching {
            viewModel.checkShop()
            viewModel.shopInfo()
            onClickWelcome()
            setupAvatarPicker()
            setupCreateShopButton()
            setupBanner()
            setupProductManagement()
            onClickApproveProduct()
            onClickLockShop()
            viewModel.getMyShopStats()
            viewModel.loadBannerItems()

            openDashboardDetail()
        }.onFailure { e ->
            println("❌ Error in initView: ${e.message}")
        }
    }

    private fun onClickLockShop() {
        binding.btLockShop.setOnClickListener {
            val intent = Intent(requireActivity(), MyShopDetailActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onClickApproveProduct() {
        binding.cvProductApproval.setOnClickListener {
            val intent = Intent(requireActivity(), ApproveProductActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openDashboardDetail() {
        binding.apply {
            // Chờ lấy hàng (position 1 - CONFIRMED)
            cvItem1.setOnClickListener {
                // ✅ Null check trước khi start activity
                if (isAdded && activity != null) {
                    try {
                        val intent = ShopDashboardDetailActivity.createIntent(requireActivity(), 0)
                        startActivity(intent)
                        println("✅ Opened ShopDashboardDetail with position 1 (Chờ lấy hàng)")
                    } catch (e: Exception) {
                        println("❌ Error opening shop dashboard: ${e.message}")
                    }
                }
            }
            
            // Đơn hủy (position 4 - CANCELLED)
            cvItem2.setOnClickListener {
                // ✅ Null check trước khi start activity
                if (isAdded && activity != null) {
                    try {
                        val intent = ShopDashboardDetailActivity.createIntent(requireActivity(), 4)
                        startActivity(intent)
                        println("✅ Opened ShopDashboardDetail with position 4 (Đơn hủy)")
                    } catch (e: Exception) {
                        println("❌ Error opening shop dashboard: ${e.message}")
                    }
                }
            }

            // Tổng số đơn hàng (position 0 - PENDING) 
            cvItem3.setOnClickListener {
                // ✅ Null check trước khi start activity
                if (isAdded && activity != null) {
                    try {
                        val intent = ShopDashboardDetailActivity.createIntent(requireActivity(), 1)
                        startActivity(intent)
                        println("✅ Opened ShopDashboardDetail with position 0 (Chờ xác nhận)")
                    } catch (e: Exception) {
                        println("❌ Error opening shop dashboard: ${e.message}")
                    }
                }
            }

            // Đánh giá (position 3 - DELIVERED)
            cvItem4.setOnClickListener {
                // ✅ Null check trước khi start activity
                if (isAdded && activity != null) {
                    try {
                        val intent = ShopDashboardDetailActivity.createIntent(requireActivity(), 3)
                        startActivity(intent)
                        println("✅ Opened ShopDashboardDetail with position 3 (Đã giao)")
                    } catch (e: Exception) {
                        println("❌ Error opening shop dashboard: ${e.message}")
                    }
                }
            }
        }
    }


    private fun onClickWelcome() {
        binding.apply {
            btWelcome.setOnClickListener {
                viewModel.changeRole()
                Toast.makeText(context, "${viewModel.isShop.value}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Setup product management click listener
     */
    private fun setupProductManagement() {
        binding.ivFuncItem1.setOnClickListener {
            // ✅ Null check trước khi start activity
            if (isAdded && activity != null) {
                try {
                    val intent = ProductManagementActivity.createIntent(requireActivity())
                    startActivity(intent)
                    println("✅ Opened ProductManagementActivity")
                } catch (e: Exception) {
                    println("❌ Error opening product management: ${e.message}")
                }
            }
        }
    }


    override fun observeViewModel() {
        observeCombinedShopState()
        observeCreateShopState()
        observeErrorMessage()
        observeBanner()
        observeShopInfo()
        observeMyShopStats()
    }

    private fun observeMyShopStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myShopStats.collect { shopStats ->
                binding.apply {
                    // ✅ Mapping với OrderStats API mới
                    tvItem2.text = shopStats?.pendingOrders.toString() // pending_orders thay vì pending_pickup
                    tvItem1.text = shopStats?.cancelledOrders.toString() // cancelled_orders
                    tvItem3.text = shopStats?.deliveredOrders.toString() // total_orders
                    tvItem4.text = shopStats?.pendingRatings.toString() // pending_ratings thay vì ratings_count
                }
            }
        }
    }

    private fun observeShopInfo() {
        // Observe categories data using StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shopInfo.collect { shop ->
                binding.apply {
                    ivAvatar.loadAvatar(
                        avatarPath = shop?.avatar,
                        placeholder = R.drawable.avatar_blank,
                        error = R.drawable.avatar_blank,
                    )
                    tvNameDashboard.text = shop?.name
                    tvShopIdDashboard.text = "greenbuy.site/" + shop?.id
                }
            }
        }
    }

    private fun setupBanner() {
        // ✅ Null check
        if (!isAdded || activity == null) return
        
        // Setup banner adapter
        bannerAdapter = BannerAdapter { banner ->
            // Handle banner click
            println("Banner clicked: ${banner}")
            // TODO: Handle banner action
        }

        // Setup ViewPager2
        binding.bannerView.apply {
            adapter = bannerAdapter
            offscreenPageLimit = 3

            // Register page change callback để detect user scrolling
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    isUserScrolling = when (state) {
                        ViewPager2.SCROLL_STATE_DRAGGING -> {
                            stopAutoScroll()
                            true
                        }
                        ViewPager2.SCROLL_STATE_IDLE -> {
                            startAutoScroll()
                            false
                        }
                        else -> isUserScrolling
                    }
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    // ✅ Null check cho binding
                    if (isAdded && isBindingInitialized()) {
                        binding.indicatorView.onPageSelected(position)
                    }
                }
            })
        }

        // Setup indicator
        binding.indicatorView.apply {
            setSlideMode(IndicatorSlideMode.WORM)
            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
            setSliderColor(
                ContextCompat.getColor(requireContext(), R.color.grey_400),
                ContextCompat.getColor(requireContext(), R.color.green_900)
            )
            setSliderWidth(30f)
            setSliderHeight(12f)
            setSlideMode(IndicatorSlideMode.WORM)
            setupWithViewPager(binding.bannerView)
        }
    }


    private fun observeCombinedShopState() {
        // Observe product data
        viewLifecycleOwner.lifecycleScope.launch {

            //Combined 2 StateFlows thanh 1
            combine(
                viewModel.isShop,
                viewModel.isShopInfo
            ) { isShop, isShopInfo ->
                ShopUiState(isShop, isShopInfo)
            }.collect { uiState ->
                handleShopState(uiState)
            }
        }
    }

    private fun handleShopState(uiState: ShopUiState) {
        println("🔄 Shop state: isShop=${uiState.isShop}, isShopInfo=${uiState.isShopInfo}")

        when {
            uiState.isShop == Role.BUYER -> {
                // Buyer
                setUpViews(1)
                println("👤 User is buyer - showing welcome")
            }
            uiState.isShop == Role.SELLER && uiState.isShopInfo == false -> {
                // Seller but no shop info
                setUpViews(2)
                println("🏪 User is seller - showing create shop")
            }
            uiState.isShop == Role.SELLER && uiState.isShopInfo == true -> {
                // Seller with shop info
                viewModel.shopInfo()
                viewModel.getMyShopStats()
                setUpViews(3)
                println("📊 User has shop - showing dashboard")
            }
            (uiState.isShop == Role.ADMIN || uiState.isShop == Role.MODERATOR) && uiState.isShopInfo == false -> {
                setUpViews(4)
                println("🔧 User is admin or moderator - showing approve")
            }
            else -> {
                // Loading or null states
//                showLoadingState()
                println("⏳ Loading shop information...")
            }
        }
    }
    
    /**
     * Observe create shop state
     */
    private fun observeCreateShopState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createShopState.collect { state ->
                when (state) {
                    is CreateShopUiState.Idle -> {
                        // Reset UI
                        binding.btRegister.isEnabled = true
                        binding.btRegister.text = "Đăng ký"
                    }
                    is CreateShopUiState.Loading -> {
                        // Show loading
                        binding.btRegister.isEnabled = false
                        binding.btRegister.text = "Đang tạo..."
                        println("⏳ Creating shop...")
                    }
                    is CreateShopUiState.Success -> {
                        // Success
                        binding.btRegister.isEnabled = true
                        binding.btRegister.text = "Đăng ký"
                        Toast.makeText(context, "✅ Tạo shop thành công!", Toast.LENGTH_LONG).show()
                        
                        // Clear form
                        clearForm()
                        
                        println("✅ Shop created: ${state.shop.name}")
                    }
                    is CreateShopUiState.Error -> {
                        // Error
                        binding.btRegister.isEnabled = true
                        binding.btRegister.text = "Đăng ký"
                        Toast.makeText(context, "❌ ${state.message}", Toast.LENGTH_LONG).show()
                        println("❌ Create shop error: ${state.message}")
                    }
                }
            }
        }
    }
    
    /**
     * Observe error messages
     */
    private fun observeErrorMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                if (!message.isNullOrEmpty()) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    viewModel.clearErrorMessage()
                }
            }
        }
    }
    
    /**
     * Clear form after successful creation
     */
    private fun clearForm() {
        binding.apply {
            etName.text?.clear()
            etPhoneNumber.text?.clear()
            cbRegister.isChecked = false
            ivAvatar.setImageResource(R.drawable.avatar_blank)
        }
        selectedAvatarUri = null
        // ✅ Clear avatar error state
        setAvatarError(false)
    }

    private fun setUpViews(viewId: Int) {
        when (viewId) {
            1 -> {
                binding.apply {
                    clWelcome.visibility = View.VISIBLE
                    clShopCreate.visibility = View.GONE
                    clDashboard.visibility = View.GONE
                    clAdmin.visibility = View.GONE
                }
            }
            2 -> {
                binding.apply {
                    clWelcome.visibility = View.GONE
                    clShopCreate.visibility = View.VISIBLE
                    clDashboard.visibility = View.GONE
                    clAdmin.visibility = View.GONE
                }
            }
            3 -> {
                binding.apply {
                    clWelcome.visibility = View.GONE
                    clShopCreate.visibility = View.GONE
                    clDashboard.visibility = View.VISIBLE
                    clAdmin.visibility = View.GONE
                }
            }
            4 -> {
                binding.apply {
                    clWelcome.visibility = View.GONE
                    clShopCreate.visibility = View.GONE
                    clDashboard.visibility = View.GONE
                    clAdmin.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Setup avatar picker click listeners
     */
    private fun setupAvatarPicker() {
        binding.apply {
            // Click vào avatar hoặc icon add để mở photo picker
            ivAvatar.setOnClickListener { openPhotoPicker() }
            icAdd.setOnClickListener { openPhotoPicker() }
        }
    }
    
    /**
     * Setup create shop button
     */
    private fun setupCreateShopButton() {
        binding.btRegister.setOnClickListener {
            createShop()
        }
    }
    
    /**
     * Mở photo picker để chọn ảnh
     */
    private fun openPhotoPicker() {
        photoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
    
    /**
     * Hiển thị/ẩn viền đỏ cho avatar khi có lỗi
     */
    private fun setAvatarError(hasError: Boolean) {
        if (hasError) {
            // Tạo viền đỏ cho avatar
            val errorBorder = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setStroke(6, ContextCompat.getColor(requireContext(), R.color.red))
                setColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            }
            binding.ivAvatar.background = errorBorder
            
            Toast.makeText(context, "❌ Vui lòng chọn ảnh avatar cho cửa hàng", Toast.LENGTH_SHORT).show()
            println("❌ Avatar validation failed - no image selected")
        } else {
            // Xóa viền đỏ
            binding.ivAvatar.background = null
        }
    }

    /**
     * Xử lý ảnh được chọn từ photo picker
     */
    private fun handleSelectedImage(uri: Uri?) {
        if (uri != null) {
            selectedAvatarUri = uri
            
            // ✅ Xóa error state khi đã chọn avatar
            setAvatarError(false)
            
            // ✅ Hiển thị ảnh lên avatar sử dụng ViewExt - dùng loadUrl cho local Uri
            binding.ivAvatar.loadUrl(
                imageUrl = uri.toString(),
                placeholder = R.drawable.avatar_blank,
                error = R.drawable.avatar_blank,
                transform = ImageTransform.CIRCLE
            )
            
            println("📸 Avatar selected: $uri")
            Toast.makeText(context, "✅ Đã chọn ảnh avatar", Toast.LENGTH_SHORT).show()
        } else {
            println("❌ No image selected")
            Toast.makeText(context, "❌ Không chọn ảnh nào", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Tạo shop với thông tin đã nhập
     */
    private fun createShop() {
        val name = binding.etName.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val isAgreed = binding.cbRegister.isChecked
        
        // ✅ Validation bao gồm cả avatar
        when {
            name.isEmpty() -> {
                binding.etName.error = "Vui lòng nhập tên cửa hàng"
                return
            }
            phoneNumber.isEmpty() -> {
                binding.etPhoneNumber.error = "Vui lòng nhập số điện thoại"
                return
            }
            selectedAvatarUri == null -> {
                setAvatarError(true)
                return
            }
            !isAgreed -> {
                Toast.makeText(context, "Vui lòng đồng ý với điều khoản", Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        // ✅ Clear avatar error nếu validation passed
        setAvatarError(false)
        
        // ✅ Gọi API tạo shop
        viewModel.createShop(
            context = requireContext(),
            name = name,
            phoneNumber = phoneNumber,
            avatarUri = selectedAvatarUri
        )
        
        println("🏪 Creating shop: name=$name, phone=$phoneNumber, avatar=${selectedAvatarUri != null}")
    }
    

    

    private fun observeBanner() {
        // ✅ Null check trước khi observe
        if (!isAdded) return
        
        // Observe banner items
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bannerItems.collect { bannerItems ->
                // ✅ Null check trong collect
                if (isAdded && isBindingInitialized() && ::bannerAdapter.isInitialized) {
                    bannerAdapter.submitList(bannerItems)

                    // Setup indicator với số lượng items
                    if (bannerItems.isNotEmpty()) {
                        binding.indicatorView.setPageSize(bannerItems.size)
                        startAutoScroll()
                    }

                    println("Banner items updated: ${bannerItems.size}")
                }
            }
        }
    }
    
    /**
     * ✅ Bắt đầu auto scroll với Handler thay vì Timer
     */
    private fun startAutoScroll() {
        // ✅ Null checks
        if (!isAdded || activity == null || !isBindingInitialized() || !::bannerAdapter.isInitialized) {
            return
        }
        
        stopAutoScroll() // Stop existing handler first

        bannerRunnable = object : Runnable {
            override fun run() {
                try {
                    // ✅ Kiểm tra lifecycle trước khi update UI
                    if (isAdded && activity != null && !isUserScrolling && 
                        isBindingInitialized() && bannerAdapter.itemCount > 0) {
                        
                        val currentItem = binding.bannerView.currentItem
                        val nextItem = (currentItem + 1) % bannerAdapter.itemCount
                        binding.bannerView.setCurrentItem(nextItem, true)
                        
                        // ✅ Schedule next scroll
                        bannerHandler.postDelayed(this, 3000) // 3 giây
                    }
                } catch (e: Exception) {
                    println("❌ Error in banner auto scroll: ${e.message}")
                }
            }
        }
        
        bannerRunnable?.let { runnable ->
            bannerHandler.postDelayed(runnable, 3000)
        }
    }

    /**
     * ✅ Dừng auto scroll với Handler
     */
    private fun stopAutoScroll() {
        bannerRunnable?.let { runnable ->
            bannerHandler.removeCallbacks(runnable)
        }
        bannerRunnable = null
    }
    
    // ✅ Thêm lifecycle methods để quản lý timer
    override fun onResume() {
        super.onResume()
        if (::bannerAdapter.isInitialized && bannerAdapter.itemCount > 0) {
            startAutoScroll()
        }
    }
    
    override fun onPause() {
        super.onPause()
        stopAutoScroll()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoScroll()
        // ✅ Clear handler để tránh memory leak
        bannerHandler.removeCallbacksAndMessages(null)
    }

}

// ✅ Data class cho UI state
data class ShopUiState(
    val isShop: Role?,
    val isShopInfo: Boolean?
) {
    val isLoading: Boolean get() = isShop == Role.BUYER || isShopInfo == null
}