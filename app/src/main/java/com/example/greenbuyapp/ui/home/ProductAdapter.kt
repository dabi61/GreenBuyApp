package com.example.greenbuyapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.databinding.ItemProductBinding
import java.text.NumberFormat
import java.util.*

/**
 * ✅ MODERN ProductAdapter: Support List<Product> cho StateFlow architecture
 * Replaced PagedListAdapter với ListAdapter để flexible hơn
 */
class ProductAdapter(
    private val onProductClick: (Product) -> Unit = {}
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        println("🏗️ ProductAdapter onCreateViewHolder called")
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        println("🔗 ProductAdapter onBindViewHolder: position=$position, product=${product.name}")
        holder.bind(product, onProductClick)
    }
    
    // ✅ Remove excessive logging in getItemCount
    override fun getItemCount(): Int {
        return super.getItemCount()
    }
    
    override fun submitList(list: List<Product>?) {
        println("📝 ProductAdapter submitList called with ${list?.size ?: 0} items")
        println("📝 Previous list size: ${currentList.size}")
        super.submitList(list)
    }
    
    override fun submitList(list: List<Product>?, commitCallback: Runnable?) {
        println("📝 ProductAdapter submitList with callback called with ${list?.size ?: 0} items")
        println("📝 Previous list size: ${currentList.size}")
        super.submitList(list, commitCallback)
    }
    
    // ✅ Thêm method để debug pagination
    fun getCurrentItemCount(): Int {
        return currentList.size
    }
    
    fun getLastItemPosition(): Int {
        return currentList.size - 1
    }


    class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product, onProductClick: (Product) -> Unit) {
            binding.apply {
                // Set product name
                tvName.text = product.name

                // Format and set price
                val formattedPrice = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                    .format(product.price.toLong())
                tvPrice.text = formattedPrice

                // ✅ Cải thiện load product image với error handling tốt hơn
                val imageView = binding.root.findViewById<android.widget.ImageView>(R.id.img_product)
                
                try {
                    if (!product.cover.isNullOrEmpty()) {
                        val imageUrl = if (product.cover.startsWith("http")) {
                            product.cover
                        } else {
                            "https://www.utt-school.site${product.cover}"
                        }
                        
                        println("🖼️ Loading image: $imageUrl")
                        
                        Glide.with(itemView.context)
                            .load(imageUrl)
                            .placeholder(R.drawable.pic_item_product)
                            .error(R.drawable.pic_item_product)
                            .timeout(10000) // 10 giây timeout
                            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                                override fun onLoadFailed(
                                    e: com.bumptech.glide.load.engine.GlideException?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    println("❌ Failed to load image: $imageUrl")
                                    println("❌ Error: ${e?.message}")
                                    return false // Let Glide handle the error
                                }

                                override fun onResourceReady(
                                    resource: android.graphics.drawable.Drawable?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                                    dataSource: com.bumptech.glide.load.DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    println("✅ Successfully loaded image: $imageUrl")
                                    return false
                                }
                            })
                            .into(imageView)
                    } else {
                        // Use placeholder image
                        imageView.setImageResource(R.drawable.pic_item_product)
                        println("🖼️ Using placeholder image for product: ${product.name}")
                    }
                } catch (e: Exception) {
                    println("❌ Exception loading image for product ${product.name}: ${e.message}")
                    imageView.setImageResource(R.drawable.pic_item_product)
                }

                // Set click listener
                root.setOnClickListener {
                    onProductClick(product)
                }

                // Hide sold info for now (we don't have this data from API)
//                tvSelled.text = ""
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.product_id == newItem.product_id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
} 