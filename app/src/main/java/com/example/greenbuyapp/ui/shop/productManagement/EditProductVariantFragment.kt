package com.example.greenbuyapp.ui.shop.productManagement

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.ProductAttribute
import com.example.greenbuyapp.databinding.FragmentEditProductVariantBinding
import com.example.greenbuyapp.ui.shop.addProduct.AddProductViewModel
import com.example.greenbuyapp.ui.shop.addProduct.DeleteAttributeUiState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProductVariantFragment : Fragment() {

    private var _binding: FragmentEditProductVariantBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddProductViewModel by viewModel()
    private lateinit var attributeAdapter: EditAttributeAdapter
    private var productId: Int = -1
    private var currentImagePickerPosition: Int = -1

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            if (currentImagePickerPosition != -1) {
                // Convert URI to file path or handle image upload
                val imagePath = selectedUri.toString()
                attributeAdapter.updateAttributeImage(currentImagePickerPosition, imagePath)
                currentImagePickerPosition = -1
            }
        }
    }

    companion object {
        private const val ARG_PRODUCT_ID = "product_id"
        
        fun newInstance(productId: Int): EditProductVariantFragment {
            return EditProductVariantFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PRODUCT_ID, productId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProductVariantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        productId = arguments?.getInt(ARG_PRODUCT_ID, -1) ?: -1
        if (productId == -1) {
            Toast.makeText(context, "Lỗi: Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }
        
        // ✅ Set productId in ViewModel trước khi thực hiện bất kỳ operation nào
        viewModel.setProductId(productId)
        println("🏷️ EditProductVariantFragment: Set productId = $productId in ViewModel")
        
        setupRecyclerView()
        setupFab()
        observeViewModel()
        
        // Load existing attributes
        loadProductAttributes()
    }

    private fun setupRecyclerView() {
        attributeAdapter = EditAttributeAdapter(
            onPickImage = { position -> openImagePicker(position) },
            onDeleteAttribute = { attribute, position -> deleteAttribute(attribute, position) },
            onSaveAttribute = { attribute, position -> saveAttribute(attribute, position) }
        )
        
        binding.rvAttributes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = attributeAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddAttribute.setOnClickListener {
            addNewAttribute()
        }
    }

    private fun loadProductAttributes() {
        viewModel.loadProductAttributes(productId)
    }

    private fun addNewAttribute() {
        // Add empty attribute to adapter
        attributeAdapter.addEmptyAttribute()
        binding.tvEmptyState.visibility = View.GONE
    }

    private fun deleteAttribute(attribute: ProductAttribute, position: Int) {
        println("🗑️ Deleting attribute:")
        println("   - attribute_id: ${attribute.attribute_id}")
        println("   - position: $position")
        println("   - current productId in fragment: $productId")

        // ✅ Phân biệt 2 trường hợp: attribute đã save hoặc chưa save
        if (attribute.attribute_id > 0) {
            // Attribute đã có trên server - gọi API delete
            println("🗑️ Deleting saved attribute from server")
            viewModel.deleteProductAttribute(attribute.attribute_id)
        } else {
            // Attribute chưa save - chỉ xóa khỏi adapter
            println("🗑️ Removing unsaved attribute from adapter")
        attributeAdapter.removeAttribute(position)
            
            // Hiển thị thông báo cho unsaved attribute
            Toast.makeText(context, "Đã hủy thuộc tính chưa lưu", Toast.LENGTH_SHORT).show()
            
            // Cập nhật empty state
            if (attributeAdapter.itemCount == 0) {
                Log.d("EditProductVariantFragment", "No attributes left, showing empty state")
                binding.tvEmptyState.visibility = View.VISIBLE
            } else {
                Log.d("EditProductVariantFragment", "${attributeAdapter.itemCount} attributes left")
                binding.tvEmptyState.visibility = View.GONE
            }
        }
    }
    


    private fun openImagePicker(position: Int) {
        currentImagePickerPosition = position
        imagePickerLauncher.launch("image/*")
    }

    private fun saveAttribute(attribute: ProductAttribute, position: Int) {
        // Validate attribute data
        if (attribute.color.isBlank() && attribute.size.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập ít nhất màu sắc hoặc kích thước", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (attribute.price <= 0) {
            Toast.makeText(context, "Vui lòng nhập giá hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (attribute.quantity < 0) {
            Toast.makeText(context, "Vui lòng nhập số lượng hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if user selected a new image
        val hasNewImage = attributeAdapter.hasNewImage(position)
        val newImageUri = attributeAdapter.getNewImageUri(position)
        
        println("🔍 DEBUG saveAttribute:")
        println("   - position: $position")
        println("   - hasNewImage: $hasNewImage")
        println("   - newImageUri: $newImageUri")
        println("   - attribute.image: ${attribute.image}")
        println("   - attribute.attribute_id: ${attribute.attribute_id}")
        
        // ✅ Phân biệt 2 trường hợp: có ảnh mới hoặc chỉ edit text
        if (hasNewImage && !newImageUri.isNullOrEmpty()) {
            // User đã chọn ảnh mới - gọi saveProductAttribute với ảnh
            println("🏭 Calling saveProductAttribute WITH new image")
            viewModel.saveProductAttribute(requireContext(), attribute, productId, true, newImageUri)
            println("🏭 Saving attribute with new image: color=${attribute.color}, size=${attribute.size}")
        } else {
            // User không chọn ảnh mới - chỉ edit text fields
            println("🏭 Calling saveProductAttribute WITHOUT new image")
            viewModel.saveProductAttribute(requireContext(), attribute, productId, false, null)
            println("🏭 Saving attribute without new image: color=${attribute.color}, size=${attribute.size}")
        }
        
        Toast.makeText(context, "Đã lưu thuộc tính thành công", Toast.LENGTH_SHORT).show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productAttributes.collect { attributes ->
                attributeAdapter.submitList(attributes)
                binding.tvEmptyState.visibility = if (attributes.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { errorMessage ->
                if (!errorMessage.isNullOrEmpty()) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    viewModel.clearErrorMessage()
                }
            }
        }
        
        // ✅ Observe delete attribute state (chỉ cho saved attributes)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteAttributeState.collect { state ->
                when (state) {
                    is DeleteAttributeUiState.Loading -> {
                        // Có thể hiển thị loading indicator nếu cần
                        println("🔄 Deleting saved attribute from server...")
                    }
                    is DeleteAttributeUiState.Success -> {
                        Toast.makeText(context, "Đã xóa thuộc tính thành công", Toast.LENGTH_SHORT).show()
                        println("✅ Saved attribute deleted successfully: ${state.attributeId}")
                        // ✅ Server sẽ tự động cập nhật danh sách thông qua loadProductAttributes
                        viewModel.resetDeleteAttributeState()
                    }
                    is DeleteAttributeUiState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        println("❌ Error deleting saved attribute: ${state.message}")
                        viewModel.resetDeleteAttributeState()
                    }
                    is DeleteAttributeUiState.Idle -> {
                        // Reset state
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 