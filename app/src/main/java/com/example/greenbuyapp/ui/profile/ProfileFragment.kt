package com.example.greenbuyapp.ui.profile

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.social.model.FollowStatsResponse
import com.example.greenbuyapp.databinding.FragmentProfileBinding
import com.example.greenbuyapp.data.user.model.UserMe
import com.example.greenbuyapp.ui.base.BaseFragment
import com.example.greenbuyapp.ui.cart.CartActivity
import com.example.greenbuyapp.ui.login.LoginActivity
import com.example.greenbuyapp.ui.main.MainActivity
import com.example.greenbuyapp.ui.profile.editProfile.EditProfileActivity
import com.example.greenbuyapp.ui.profile.orders.CustomerOrderActivity
import com.example.greenbuyapp.util.Result
import com.example.greenbuyapp.util.loadAvatar
import com.example.greenbuyapp.util.clearImage
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment hiển thị màn hình hồ sơ người dùng
 */
class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileViewModel>() {

    private lateinit var utilAdapter: UtilAdapter
    
    // Dialog references để manage lifecycle
    private var tokenExpiredDialog: AlertDialog? = null
    private var loginRequiredDialog: AlertDialog? = null
    private var logoutDialog: AlertDialog? = null

    override val viewModel: ProfileViewModel by viewModel()
    override fun getLayoutResourceId(): Int = R.layout.fragment_profile

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        try {
            setUpUtilProfile()
            setupClickListeners()
            setupLogoutAction()
            viewModel.loadUtilProfile()
            // Setup cart button click
            binding.icCart.setOnClickListener {
                val intent = CartActivity.createIntent(requireContext())
                startActivity(intent)
                println("🛒 Opening CartActivity")
            }
            // Kiểm tra auth status và load user profile
            viewModel.checkAuthStatus()
        }
        catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override fun observeViewModel() {
        observeUtilProfile()
        observeAuthState()
        observeUserProfile()
        observeFollowStats()
        observeLoading()
        observeTokenExpiredEvents()
    }
    
    override fun onDestroyView() {
        // Dismiss tất cả dialogs để tránh window leak
        dismissAllDialogs()
        super.onDestroyView()
    }
    
    private fun dismissAllDialogs() {
        try {
            tokenExpiredDialog?.dismiss()
            tokenExpiredDialog = null
            
            loginRequiredDialog?.dismiss()
            loginRequiredDialog = null
            
            logoutDialog?.dismiss()
            logoutDialog = null
            
            println("🧹 All dialogs dismissed to prevent window leak")
        } catch (e: Exception) {
            println("⚠️ Error dismissing dialogs: ${e.message}")
        }
    }
    
//    override fun isFragmentSafe(): Boolean {
//        return isAdded && !isRemoving && !isDetached && activity != null && !requireActivity().isFinishing
//    }


    private fun setUpUtilProfile() {
        // Setup util adapter
        utilAdapter = UtilAdapter { utilProfile ->
            // Handle util item click
            handleUtilItemClick(utilProfile)
        }

        binding.rvUtil.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = utilAdapter
        }
    }
    
    /**
     * Setup click listeners for UI elements
     */
    private fun setupClickListeners() {
        // Click listener cho "Xem lịch sử mua hàng"
        binding.tvHistory.setOnClickListener {
            navigateToCustomerOrders()
        }
        
        // Click listeners cho các icon trạng thái đơn hàng
        binding.ivConfirm.setOnClickListener {
            navigateToCustomerOrders(0) // CONFIRMED tab - "Đã xác nhận"
        }
        
        binding.ivWait.setOnClickListener {
            navigateToCustomerOrders(2) // SHIPPED tab - "Đang giao" 
        }
        
        binding.ivShipping.setOnClickListener {
            navigateToCustomerOrders(3) // DELIVERED tab - "Đã giao"
        }
        
        // Click listeners cho text labels (optional)
        binding.tvItem1.setOnClickListener {
            navigateToCustomerOrders(0) // PENDING tab - "Chờ xác nhận"
        }
        
        binding.tvItem2.setOnClickListener {
            navigateToCustomerOrders(2) // CONFIRMED tab - "Đã xác nhận"
        }
        
        binding.tvItem3.setOnClickListener {
            navigateToCustomerOrders(3) // SHIPPED tab - "Đang giao"
        }
        
    
    }
    
    /**
     * Navigate to Customer Orders Activity
     */
    private fun navigateToCustomerOrders(position: Int = 0) {
        // Check if user is authenticated
        if (viewModel.authState.value is AuthState.Authenticated) {
            try {
                val intent = CustomerOrderActivity.createIntent(requireContext(), position)
                startActivity(intent)
                println("📱 Navigating to customer orders at position: $position")
            } catch (e: Exception) {
                println("❌ Error navigating to customer orders: ${e.message}")
                showError("Lỗi khi mở trang đơn hàng")
            }
        } else {
            showLoginRequiredDialog()
        }
    }
    
    /**
     * Handle click on util items
     */
    private fun handleUtilItemClick(utilProfile: UtilProfile) {
        when (utilProfile.title) {
            "Yêu thích" -> {
                // Navigate to favorites
                println("📱 Navigating to favorites")
            }
            "Đánh giá của tôi" -> {
                // Navigate to reviews
                println("📱 Navigating to reviews")
            }
            "Tư cách thành viên" -> {
                // Navigate to membership
                println("📱 Navigating to membership")
            }
            "Trung tâm trợ giúp" -> {
                // Navigate to help center
                println("📱 Navigating to help center")
            }
            "Top cửa hàng" -> {
                // Navigate to top shops
                println("📱 Navigating to top shops")
            }
            "Cửa hàng theo dõi" -> {
                // Navigate to followed shops
                println("📱 Navigating to followed shops")
            }
            "Giỏ hàng" -> {
                // Navigate to cart
                println("📱 Navigating to cart")
            }
            "Chat" -> {
                // Navigate to chat
                println("📱 Navigating to chat")
            }
            "Khuyến mãi" -> {
                // Navigate to promotions
                println("📱 Navigating to promotions")
            }
            else -> {
                println("📱 Unknown util item: ${utilProfile.title}")
            }
        }
    }

    private fun observeUtilProfile() {
        // Observe util items
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.utilProfile.collect { utilItems ->
                utilAdapter.submitList(utilItems)
                println("Util items updated: ${utilItems.size}")
            }
        }
    }

    private fun observeAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                // Check fragment state trước khi handle auth state
                if (!isFragmentSafe()) {
                    println("⚠️ Fragment not safe, skipping auth state handling")
                    return@collect
                }
                
                when (state) {
                    is AuthState.Unknown -> {
                        println("🔄 Auth state: Unknown")
                        // Initial state, do nothing
                    }
                    is AuthState.Authenticated -> {
                        println("✅ Auth state: Authenticated - loading profile data...")
                        
                        // Serialize việc load data thay vì gọi đồng thời để tránh race condition
                        loadProfileDataSequentially()
                    }
                    is AuthState.NotAuthenticated -> {
                        println("❌ Auth state: Not Authenticated")
                        // User not authenticated, redirect to login
                        showLoginRequiredDialog()
                    }
                }
            }
        }
    }
    
    /**
     * Load profile data một cách tuần tự để tránh race condition
     */
    private fun loadProfileDataSequentially() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Load user profile trước
                viewModel.loadUserProfile()
                
                // Đợi user profile complete, sau đó load follow stats
                // Có thể thêm delay nhỏ để đảm bảo API call trước hoàn thành
                kotlinx.coroutines.delay(500)
                viewModel.loadFollowStats()
                
            } catch (e: Exception) {
                println("💥 Error loading profile data: ${e.message}")
            }
        }
    }

    private fun observeUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userProfile.collect { result ->
                when (result) {
                    is Result.Success -> {
                        bindUserData(result.value)
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

    private fun observeFollowStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.followStats.collect { result ->
                println("📊 ObserveFollowStats received: $result")
                when (result) {
                    is Result.Success -> {
                        println("🎯 Fragment: FollowStats success, calling bindFollowStats")
                        bindFollowStats(result.value)
                    }
                    is Result.Error -> {
                        println("❌ Fragment: FollowStats error - ${result.error}")
                        showError("Lỗi khi tải thông tin follow: ${result.error ?: "Lỗi không xác định"}")
                    }
                    is Result.NetworkError -> {
                        println("🌐 Fragment: FollowStats network error")
                        showError("Lỗi mạng, vui lòng kiểm tra kết nối internet")
                    }
                    is Result.Loading -> {
                        println("⏳ Fragment: FollowStats loading")
                        // Handle loading state if needed
                    }
                    null -> {
                        println("🔄 Fragment: FollowStats initial state (null)")
                        // Initial state, do nothing
                    }
                }
            }
        }
    }

    private fun observeLoading() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // Có thể hiển thị loading indicator ở đây
                // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                
                // Disable/enable interactions during loading
                setUIEnabled(!isLoading)
            }
        }
    }

    private fun observeTokenExpiredEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tokenExpiredEvent.collect { event ->
                event?.let {
                    // Check fragment state trước khi show dialog
                    if (isFragmentSafe()) {
                        if (it.shouldShowDialog) {
                            showTokenExpiredDialog(it.message)
                        } else {
                            showError(it.message)
                            navigateToLogin()
                        }
                        viewModel.clearTokenExpiredEvent()
                    } else {
                        println("⚠️ Fragment not safe, skipping dialog show")
                    }
                }
            }
        }
    }

    private fun bindUserData(user: UserMe) {
        binding.apply {
            // Hiển thị tên đầy đủ hoặc username
            tvUsername.text = getDisplayName(user)

            // Hiển thị phone number với formatting
//            tvFollower.text = formatPhoneNumber(user.phone_number)
//            tvTitleFollower.text = "Số điện thoại"

            // Hiển thị email
//            tvFollowing.text = user.email
//            tvTitleFollowing.text = "Email"

            // Hiển thị avatar với improved logging
            println("🖼️ ProfileFragment: Loading avatar")
            println("   Avatar path: ${user.avatar}")
            println("   Avatar null/empty: ${user.avatar.isNullOrEmpty()}")
            
            if (!user.avatar.isNullOrEmpty()) {
                // Clear cache trước khi load ảnh mới để đảm bảo update
                binding.ivAvatar.clearImage(R.drawable.avatar_blank)
                
                binding.ivAvatar.loadAvatar(
                    avatarPath = user.avatar,
                    placeholder = R.drawable.avatar_blank,
                    error = R.drawable.avatar_blank,
                    forceRefresh = true // ✅ Force refresh để bypass cache
                )
                println("✅ Avatar loading initiated for: ${user.avatar}")
            } else {
                println("⚠️ No avatar URL, using default")
                binding.ivAvatar.setImageResource(R.drawable.avatar_blank)
            }
        }
        
        // Log thông tin để debug
        println("✅ User profile loaded: ${getDisplayName(user)} - ${user.role}")
        println("📱 Phone: ${user.phone_number}, 🎂 Birth: ${user.birth_date}")
        println("✅ Verified: ${user.is_verified}, 🌟 Active: ${user.is_active}")
        println("🔗 Avatar URL: ${user.avatar}")
    }

    private fun bindFollowStats(followStats: FollowStatsResponse) {
        binding.apply {
            // Hiển thị tên đầy đủ hoặc username
            tvFollower.text = (followStats.followers_count + followStats.my_shop_followers_count).toString()
            tvFollowing.text = (followStats.following_count + followStats.shop_following_count).toString()
        }

        // Log thông tin để debug
        println("✅ User followStats loaded: ${followStats}")
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

    private fun formatPhoneNumber(phoneNumber: String?): String {
        return if (phoneNumber.isNullOrBlank()) {
            "Chưa cập nhật"
        } else {
            // Format phone number for Vietnam: 0379396103 -> 037 939 6103
            if (phoneNumber.length == 10 && phoneNumber.startsWith("0")) {
                "${phoneNumber.substring(0, 3)} ${phoneNumber.substring(3, 6)} ${phoneNumber.substring(6)}"
            } else {
                phoneNumber
            }
        }
    }



    private fun formatBirthDate(birthDate: String?): String {
        return try {
            if (birthDate.isNullOrBlank()) return "Chưa cập nhật"
            
            // Parse ISO date: "2004-01-06T00:00:00"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(birthDate)
            
            if (date != null) {
                outputFormat.format(date)
            } else {
                "Chưa cập nhật"
            }
        } catch (e: Exception) {
            println("❌ Error parsing birth date: $birthDate - ${e.message}")
            "Chưa cập nhật"
        }
    }

    private fun getRoleDisplayName(role: String): String {
        return when (role.lowercase()) {
            "seller" -> "Người bán"
            "buyer" -> "Người mua"
            "admin" -> "Quản trị viên"
            "moderator" -> "Điều hành viên"
            else -> role.replaceFirstChar { it.uppercase() }
        }
    }

//    private fun showError(message: String) {
//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//    }

    private fun setUIEnabled(enabled: Boolean) {
        binding.apply {
            // Có thể disable/enable các UI elements khi loading
            // rvUtil.isEnabled = enabled
            // ivSetting.isEnabled = enabled
        }
    }

    private fun showLoginRequiredDialog() {
        // Check fragment state
        if (!isFragmentSafe()) {
            println("⚠️ Fragment not safe, cannot show login dialog")
            return
        }
        
        // Dismiss existing dialog
        loginRequiredDialog?.dismiss()
        
        loginRequiredDialog = AlertDialog.Builder(requireContext())
            .setTitle("Yêu cầu đăng nhập")
            .setMessage("Bạn cần đăng nhập để xem thông tin cá nhân. Đăng nhập ngay để trải nghiệm đầy đủ tính năng.")
            .setPositiveButton("Đăng nhập") { _, _ ->
                navigateToLogin()
            }
            .setNegativeButton("Về trang chủ") { dialog, _ ->
                dialog.dismiss()
                // Navigate về Home tab
                navigateToHome()
            }
            .setCancelable(false)
            .setOnDismissListener {
                loginRequiredDialog = null
            }
            .show()
    }

    private fun showTokenExpiredDialog(message: String) {
        // Check fragment state
        if (!isFragmentSafe()) {
            println("⚠️ Fragment not safe, cannot show token expired dialog")
            return
        }
        
        // Dismiss existing dialog
        tokenExpiredDialog?.dismiss()
        
        tokenExpiredDialog = AlertDialog.Builder(requireContext())
            .setTitle("Phiên đăng nhập hết hạn")
            .setMessage(message)
            .setPositiveButton("Đăng nhập lại") { _, _ ->
                navigateToLogin()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .setOnDismissListener {
                tokenExpiredDialog = null
            }
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun navigateToHome() {
        // Switch to Home tab (position 0) trong bottom navigation
        (requireActivity() as? MainActivity)?.let { mainActivity ->
            // Reset bottom navigation về Home
            // mainActivity.binding.bottomNavigation.itemActiveIndex = 0
        }
        // Có thể sử dụng fragment manager để pop back stack về home
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun setupLogoutAction() {
        // Có thể thêm logout button vào menu hoặc profile UI
        binding.ivSetting.setOnClickListener {
//            showLogoutDialog()
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }
    }



    override fun onResume() {
        super.onResume()
        // Force refresh profile để đảm bảo avatar mới được load
        forceRefreshProfile()
        
        val mainActivity = requireActivity() as? MainActivity
        if (mainActivity?.pendingOpenOrders == true) {
            mainActivity.pendingOpenOrders = false
            navigateToCustomerOrders(4)
        }
    }
    
    /**
     * Force refresh profile data - bypass cache
     */
    private fun forceRefreshProfile() {
        println("🔄 ProfileFragment: Force refreshing profile data...")
        viewModel.loadUserProfile()
        viewModel.loadFollowStats()
    }

    fun openDeliveredOrdersIfPending() {
        val mainActivity = requireActivity() as? MainActivity
        if (mainActivity?.pendingOpenOrders == true) {
            mainActivity.pendingOpenOrders = false
            navigateToCustomerOrders(4) // Tab "Đã giao"
        }
    }
}