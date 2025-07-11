package com.example.greenbuyapp.ui.shop.orderDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.shop.model.OrderItem
import com.example.greenbuyapp.databinding.ItemOrderDetailProductBinding
import com.example.greenbuyapp.util.loadUrl
import com.example.greenbuyapp.domain.product.ProductRepository
import com.example.greenbuyapp.util.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderDetailItemAdapter(
    private val productRepository: ProductRepository
) : ListAdapter<OrderItem, OrderDetailItemAdapter.OrderItemViewHolder>(OrderItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemOrderDetailProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderItemViewHolder(binding, productRepository)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderItemViewHolder(
        private val binding: ItemOrderDetailProductBinding,
        private val productRepository: ProductRepository
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderItem) {
            binding.apply {
                // Product info
                tvProductName.text = item.productName
                tvAttributeDetails.text = item.attributeDetails
                tvQuantity.text = "x${item.quantity}"
                
                // Prices
                tvUnitPrice.text = item.getFormattedUnitPrice()
                tvTotalPrice.text = item.getFormattedTotalPrice()
                
                // ✅ Enhanced Product image loading với debug logging
                println("🖼️ OrderItem image debug:")
                println("   Product ID: ${item.productId}")
                println("   Attribute ID: ${item.attributeId}")
                println("   Product name: ${item.productName}")
                println("   Raw productImage: '${item.productImage}'")
                println("   Raw attributeImage: '${item.attributeImage}'")
                println("   Is productImage null/empty: ${item.productImage.isNullOrEmpty()}")
                println("   Is attributeImage null/empty: ${item.attributeImage.isNullOrEmpty()}")
                
                // ✅ Kiểm tra nếu không có attributeImage, thì gọi API để lấy
                if (item.attributeImage.isNullOrEmpty() && item.attributeId > 0) {
                    println("   🔄 Attribute image is missing, fetching from server...")
                    
                    // Load placeholder trước
                    ivProductImage.setImageResource(R.drawable.pic_item_product)
                    
                    // Call API để lấy attribute image
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val result = withContext(Dispatchers.IO) {
                                productRepository.getAttribute(item.attributeId)
                            }
                            
                            when (result) {
                                is Result.Success -> {
                                    val attribute = result.value
                                    val attributeImageUrl = attribute.getImageUrl()
                                    println("   ✅ Attribute image loaded: $attributeImageUrl")
                                    
                                    if (attributeImageUrl.isNotEmpty()) {
                                        ivProductImage.loadUrl(
                                            imageUrl = attributeImageUrl,
                                            placeholder = R.drawable.pic_item_product,
                                            error = R.drawable.pic_item_product
                                        )
                                    } else {
                                        // Fallback to product image
                                        loadFallbackImage(item)
                                    }
                                }
                                is Result.Error -> {
                                    println("   ❌ Failed to load attribute: ${result.error}")
                                    loadFallbackImage(item)
                                }
                                is Result.Loading -> {
                                    // Keep showing placeholder
                                }
                                is Result.NetworkError -> {
                                    println("   🌐 Network error loading attribute")
                                    loadFallbackImage(item)
                                }
                            }
                        } catch (e: Exception) {
                            println("   ❌ Exception loading attribute: ${e.message}")
                            loadFallbackImage(item)
                        }
                    }
                } else {
                    // ✅ Sử dụng method getImageUrl() chuẩn với priority: attribute_image > product_image
                    val imageUrl = item.getImageUrl()
                    println("   Final imageUrl from getImageUrl(): '$imageUrl'")
                    println("   Image priority: ${if (!item.attributeImage.isNullOrEmpty()) "attribute_image" else if (!item.productImage.isNullOrEmpty()) "product_image" else "none"}")
                    
                    if (!imageUrl.isNullOrEmpty()) {
                        ivProductImage.loadUrl(
                            imageUrl = imageUrl,
                            placeholder = R.drawable.pic_item_product,
                            error = R.drawable.pic_item_product
                        )
                    } else {
                        println("   Using placeholder image")
                        ivProductImage.setImageResource(R.drawable.pic_item_product)
                    }
                }
                
                println("📦 Shop order item bound: ${item.productName} x${item.quantity} = ${item.getFormattedTotalPrice()}")
            }
        }
        
        private fun loadFallbackImage(item: OrderItem) {
            // Fallback to product image if available
            val productImageUrl = item.productImage?.let { 
                if (it.startsWith("http")) it else "https://www.utt-school.site$it"
            }
            
            if (!productImageUrl.isNullOrEmpty()) {
                binding.ivProductImage.loadUrl(
                    imageUrl = productImageUrl,
                    placeholder = R.drawable.pic_item_product,
                    error = R.drawable.pic_item_product
                )
            } else {
                binding.ivProductImage.setImageResource(R.drawable.pic_item_product)
            }
        }
    }

    class OrderItemDiffCallback : DiffUtil.ItemCallback<OrderItem>() {
        override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
            return oldItem == newItem
        }
    }
} 