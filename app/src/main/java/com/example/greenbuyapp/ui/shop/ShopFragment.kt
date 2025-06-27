package com.example.greenbuyapp.ui.shop

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.transition.Visibility
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.FragmentHomeBinding
import com.example.greenbuyapp.databinding.FragmentShopBinding
import com.example.greenbuyapp.ui.base.BaseFragment
import com.example.greenbuyapp.util.ImageTransform
import com.example.greenbuyapp.util.loadAvatar
import com.example.greenbuyapp.util.loadUrl
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Fragment hiển thị màn hình shop
 */
class ShopFragment : BaseFragment<FragmentShopBinding, ShopViewModel>() {

    override val viewModel: ShopViewModel by viewModel()
    
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
        runCatching {
            viewModel.checkShop()
            viewModel.shopInfo()

            onClickWelcome()
            setupAvatarPicker()
            setupCreateShopButton()
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


    override fun observeViewModel() {
        observeCombinedShopState()
        observeCreateShopState()
        observeErrorMessage()
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
    }

    private fun setUpViews(viewId: Int) {
        when (viewId) {
            1 -> {
                binding.apply {
                    clWelcome.visibility = View.VISIBLE
                    clShopCreate.visibility = View.GONE
                    clDashboard.visibility = View.GONE
                    clApprove.visibility = View.GONE
                }
            }
            2 -> {
                binding.apply {
                    clWelcome.visibility = View.GONE
                    clShopCreate.visibility = View.VISIBLE
                    clDashboard.visibility = View.GONE
                    clApprove.visibility = View.GONE
                }
            }
            3 -> {
                binding.apply {
                    clWelcome.visibility = View.GONE
                    clShopCreate.visibility = View.GONE
                    clDashboard.visibility = View.VISIBLE
                    clApprove.visibility = View.GONE
                }
            }
            4 -> {
                binding.apply {
                    clWelcome.visibility = View.GONE
                    clShopCreate.visibility = View.GONE
                    clDashboard.visibility = View.GONE
                    clApprove.visibility = View.VISIBLE
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
     * Xử lý ảnh được chọn từ photo picker
     */
    private fun handleSelectedImage(uri: Uri?) {
        if (uri != null) {
            selectedAvatarUri = uri
            
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
        
        // ✅ Validation
        when {
            name.isEmpty() -> {
                binding.etName.error = "Vui lòng nhập tên cửa hàng"
                return
            }
            phoneNumber.isEmpty() -> {
                binding.etPhoneNumber.error = "Vui lòng nhập số điện thoại"
                return
            }
            !isAgreed -> {
                Toast.makeText(context, "Vui lòng đồng ý với điều khoản", Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        // ✅ Gọi API tạo shop
        viewModel.createShop(
            context = requireContext(),
            name = name,
            phoneNumber = phoneNumber,
            avatarUri = selectedAvatarUri
        )
        
        println("🏪 Creating shop: name=$name, phone=$phoneNumber, avatar=${selectedAvatarUri != null}")
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Khởi tạo các view và thiết lập sự kiện
        initViews()
    }
    
    private fun initViews() {
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
    }

}

// ✅ Data class cho UI state
data class ShopUiState(
    val isShop: Role?,
    val isShopInfo: Boolean?
) {
    val isLoading: Boolean get() = isShop == Role.BUYER || isShopInfo == null
}