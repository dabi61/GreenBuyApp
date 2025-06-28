package com.example.greenbuyapp.ui.shop.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.shop.model.Order
import com.example.greenbuyapp.data.shop.model.OrderStatus
import com.example.greenbuyapp.databinding.ItemOrderBinding
import java.text.NumberFormat
import java.util.*

class OrderAdapter(
    private val onOrderClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order, onOrderClick)
    }

    class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).apply {
            currency = Currency.getInstance("VND")
        }

        fun bind(order: Order, onOrderClick: (Order) -> Unit) {
            binding.apply {
                // Order info - sử dụng data mới từ API
                tvOrderId.text = order.orderNumber // Sử dụng order_number thay vì id
                tvCustomerName.text = "Khách hàng: ${order.customerName}"
                tvOrderDate.text = "Ngày đặt: ${order.formattedCreatedAt}" // Sử dụng helper property
                tvProductCount.text = "${order.totalItems} sản phẩm" // Sử dụng total_items
                
                // Format amount
                tvTotalAmount.text = currencyFormatter.format(order.totalAmount)
                
                // Status - sử dụng orderStatus enum
                tvStatus.text = order.orderStatus.displayName
                tvStatus.backgroundTintList = ContextCompat.getColorStateList(
                    itemView.context,
                    getStatusColor(order.orderStatus)
                )
                
                // Click listener
                root.setOnClickListener {
                    onOrderClick(order)
                }
            }
        }

        private fun getStatusColor(status: OrderStatus): Int {
            return when (status) {
                OrderStatus.PENDING -> R.color.lightYellow
                OrderStatus.CONFIRMED -> R.color.lightBlue
                OrderStatus.PROCESSING -> R.color.lightPurple
                OrderStatus.SHIPPING -> R.color.lightTeal
                OrderStatus.DELIVERED -> R.color.teal
                OrderStatus.CANCELLED -> R.color.lightRed
                OrderStatus.REFUNDED -> R.color.red
                OrderStatus.RETURNED -> R.color.brown
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
} 