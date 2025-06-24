package com.example.greenbuyapp.ui.profile

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.social.model.FollowStatsResponse
import com.example.greenbuyapp.databinding.FragmentProfileBinding
import com.example.greenbuyapp.data.user.model.UserMe
import com.example.greenbuyapp.ui.base.BaseFragment
import com.example.greenbuyapp.ui.login.LoginActivity
import com.example.greenbuyapp.ui.main.MainActivity
import com.example.greenbuyapp.util.Result
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
            setupLogoutAction()
            viewModel.loadUtilProfile()
            
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
    
//    private fun isFragmentSafe(): Boolean {
//        return isAdded && !isRemoving && !isDetached && activity != null && !requireActivity().isFinishing
//    }


    private fun setUpUtilProfile() {
        // Setup banner adapter
        utilAdapter = UtilAdapter { utilProfile ->
            // Handle banner click
            println("Banner clicked: ${utilProfile}")
            // TODO: Handle banner action
        }

        binding.rvUtil.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = utilAdapter
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
                        // Initial state, do nothing
                    }
                    is AuthState.Authenticated -> {
                        // User is authenticated, load profile and follow stats
                        viewModel.loadUserProfile()
                        viewModel.loadFollowStats()
                    }
                    is AuthState.NotAuthenticated -> {
                        // User not authenticated, redirect to login
                        showLoginRequiredDialog()
                    }
                }
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

            // Hiển thị avatar
            loadUserAvatar(user.avatar)
        }
        
        // Log thông tin để debug
        println("✅ User profile loaded: ${getDisplayName(user)} - ${user.role}")
        println("📱 Phone: ${user.phone_number}, 🎂 Birth: ${user.birth_date}")
        println("✅ Verified: ${user.is_verified}, 🌟 Active: ${user.is_active}")
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

    private fun loadUserAvatar(avatarPath: String?) {
        if (!avatarPath.isNullOrEmpty()) {
            val avatarUrl = if (avatarPath.startsWith("http")) {
                avatarPath
            } else {
                "https://www.utt-school.site$avatarPath"
            }
            
            Glide.with(this@ProfileFragment)
                .load(avatarUrl)
                .placeholder(R.drawable.avatar_blank)
                .error(R.drawable.avatar_blank)
                .circleCrop() // Làm tròn avatar
                .into(binding.ivAvatar)
        } else {
            binding.ivAvatar.setImageResource(R.drawable.avatar_blank)
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
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        // Check fragment state
        if (!isFragmentSafe()) {
            println("⚠️ Fragment not safe, cannot show logout dialog")
            return
        }
        
        // Dismiss existing dialog
        logoutDialog?.dismiss()
        
        logoutDialog = AlertDialog.Builder(requireContext())
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc muốn đăng xuất?")
            .setPositiveButton("Đăng xuất") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                logoutDialog = null
            }
            .show()
    }
}