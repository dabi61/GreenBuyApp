package com.example.greenbuyapp.ui.shop.productManagement

import android.content.Context
import android.net.Uri
import android.os.Bundle
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
import com.example.greenbuyapp.domain.product.ProductRepository
import com.example.greenbuyapp.util.Result
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class EditProductVariantFragment : Fragment() {

    private var _binding: FragmentEditProductVariantBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var attributeAdapter: EditAttributeAdapter
    private val productRepository: ProductRepository by inject()
    private var productId: Int = -1
    private var attributes: List<ProductAttribute> = emptyList()

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
            Toast.makeText(context, "❌ Product ID không hợp lệ", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }
        
        setupRecyclerView()
        setupFab()
        loadAttributes()
    }

    private fun setupRecyclerView() {
        attributeAdapter = EditAttributeAdapter(
            onEditAttribute = { attribute ->
                // TODO: Mở dialog edit attribute
                Toast.makeText(context, "Chỉnh sửa: ${attribute.color} - ${attribute.size}", Toast.LENGTH_SHORT).show()
            },
            onDeleteAttribute = { attribute ->
                // TODO: Xóa attribute
                Toast.makeText(context, "Xóa: ${attribute.color} - ${attribute.size}", Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.rvAttributes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = attributeAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddAttribute.setOnClickListener {
            // TODO: Mở dialog thêm attribute mới
            Toast.makeText(context, "➕ Thêm thuộc tính mới", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAttributes() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            
            when (val result = productRepository.getProductAttributes(productId)) {
                is Result.Success -> {
                    attributes = result.value
                    attributeAdapter.submitList(attributes)
                    
                    if (attributes.isEmpty()) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvAttributes.visibility = View.GONE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvAttributes.visibility = View.VISIBLE
                    }
                    
                    println("✅ Loaded ${attributes.size} attributes for product $productId")
                }
                is Result.Error -> {
                    Toast.makeText(context, "❌ Lỗi tải thuộc tính: ${result.error}", Toast.LENGTH_LONG).show()
                    println("❌ Error loading attributes: ${result.error}")
                }
                is Result.NetworkError -> {
                    Toast.makeText(context, "❌ Lỗi kết nối mạng", Toast.LENGTH_LONG).show()
                    println("❌ Network error loading attributes")
                }
                is Result.Loading -> {
                    // Already handled by progress bar
                }
            }
            
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 