package com.example.greenbuyapp.ui.shop.productManagement

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.product.model.ProductStatus
import com.example.greenbuyapp.databinding.ItemProductManagementBinding
import com.example.greenbuyapp.util.ImageTransform
import com.example.greenbuyapp.util.loadUrl
import java.text.NumberFormat
import java.util.*

class ProductManagementAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onMoreClick: (Product) -> Unit
) : ListAdapter<Product, ProductManagementAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductManagementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemProductManagementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                // Tên sản phẩm
                tvProductName.text = product.name
                
                // Mô tả sản phẩm
                tvProductDescription.text = product.description
                
                // Giá sản phẩm
                val formattedPrice = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                    .format(product.price)
                tvProductPrice.text = formattedPrice
                
                // Ảnh sản phẩm
                val imageUrl = if (!product.cover.isNullOrEmpty()) {
                    if (product.cover.startsWith("http")) {
                        product.cover
                    } else {
                        "https://www.utt-school.site${product.cover}"
                    }
                } else {
                    null
                }
                
                ivProductImage.loadUrl(
                    imageUrl = imageUrl,
                    placeholder = R.drawable.pic_item_product,
                    error = R.drawable.pic_item_product,
                    transform = ImageTransform.ROUNDED
                )
                
                println("🖼️ ProductManagement: Loading image for ${product.name}")
                println("   Cover field: '${product.cover}'")
                println("   Final URL: '$imageUrl'")
                
                // Trạng thái sản phẩm
                setupStatusBadge(product)
                
                // Click listeners
                root.setOnClickListener { onItemClick(product) }
            }
        }
        
        private fun setupStatusBadge(product: Product) {
            val context = binding.root.context
            
            binding.tvStatus.apply {
                when {
                    !product.isApproved -> {
                        text = ProductStatus.PENDING_APPROVAL.displayName
                        setBackgroundColor(ContextCompat.getColor(context, R.color.orange))
                    }
                    product.stock_info?.status != null -> {
                        // Sử dụng status từ stock_info
                        val productStatus = ProductStatus.fromStockStatus(product.stock_info.status)
                        when (productStatus) {
                            ProductStatus.OUT_OF_STOCK -> {
                                text = ProductStatus.OUT_OF_STOCK.displayName
                                setBackgroundColor(ContextCompat.getColor(context, R.color.red))
                            }
                            ProductStatus.IN_STOCK -> {
                                text = ProductStatus.IN_STOCK.displayName
                                setBackgroundColor(ContextCompat.getColor(context, R.color.main_color))
                            }
                            ProductStatus.PENDING_APPROVAL -> {
                                text = ProductStatus.PENDING_APPROVAL.displayName
                                setBackgroundColor(ContextCompat.getColor(context, R.color.orange))
                            }
                            else -> {
                                // Fallback cho các trạng thái khác
                                text = ProductStatus.IN_STOCK.displayName  
                                setBackgroundColor(ContextCompat.getColor(context, R.color.main_color))
                            }
                        }
                    }
                    else -> {
                        // Fallback: nếu approved mà không có stock_info thì coi như còn hàng
                        text = ProductStatus.IN_STOCK.displayName  
                        setBackgroundColor(ContextCompat.getColor(context, R.color.main_color))
                    }
                }
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            val isSame = oldItem.product_id == newItem.product_id
            println("🔍 DiffCallback.areItemsTheSame: ${oldItem.product_id} == ${newItem.product_id} = $isSame")
            return isSame
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            val isSame = oldItem == newItem
            println("🔍 DiffCallback.areContentsTheSame for ${oldItem.product_id}:")
            println("   Old: name='${oldItem.name}', price=${oldItem.price}, desc='${oldItem.description}'")
            println("   New: name='${newItem.name}', price=${newItem.price}, desc='${newItem.description}'")
            println("   Is same: $isSame")
            return isSame
        }
    }
}