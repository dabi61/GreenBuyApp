package com.example.greenbuyapp.ui.profile.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.user.model.CustomerOrder
import com.example.greenbuyapp.databinding.ItemCustomerOrderBinding
import java.text.NumberFormat
import java.util.*

class CustomerOrderAdapter(
    private val onOrderClick: (CustomerOrder) -> Unit
) : ListAdapter<CustomerOrder, CustomerOrderAdapter.CustomerOrderViewHolder>(CustomerOrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerOrderViewHolder {
        val binding = ItemCustomerOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomerOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerOrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order, onOrderClick)
    }

    class CustomerOrderViewHolder(
        private val binding: ItemCustomerOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).apply {
            currency = Currency.getInstance("VND")
        }

        fun bind(order: CustomerOrder, onOrderClick: (CustomerOrder) -> Unit) {
            binding.apply {
                // Order info - sử dụng data từ CustomerOrder
                tvOrderId.text = order.orderNumber
                tvOrderDate.text = order.formattedCreatedAt
                tvProductCount.text = "${order.totalItems} sản phẩm"
                
                // Format amount
                tvTotalAmount.text = currencyFormatter.format(order.totalAmount)
                
                // Status
                tvStatus.text = order.statusDisplayName
                tvStatus.backgroundTintList = ContextCompat.getColorStateList(
                    itemView.context,
                    getStatusColor(order.status)
                )
                
                // Click listener - navigate to order detail
                root.setOnClickListener {
                    onOrderClick(order)
                }
            }
        }

        private fun getStatusColor(status: String): Int {
            return when (status) {
                "pending" -> R.color.lightYellow
                "confirmed" -> R.color.lightBlue
                "processing" -> R.color.lightPurple
                "shipped" -> R.color.lightTeal
                "delivered" -> R.color.teal
                "cancelled" -> R.color.lightRed
                "refunded" -> R.color.red
                "returned" -> R.color.brown
                else -> R.color.grey_400
            }
        }
    }

    class CustomerOrderDiffCallback : DiffUtil.ItemCallback<CustomerOrder>() {
        override fun areItemsTheSame(oldItem: CustomerOrder, newItem: CustomerOrder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CustomerOrder, newItem: CustomerOrder): Boolean {
            return oldItem == newItem
        }
    }
} 