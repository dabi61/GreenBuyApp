package com.example.greenbuyapp.ui.admin.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.data.shop.model.AdminOrder
import com.example.greenbuyapp.databinding.ItemAdminOrderBinding

class AdminOrderAdapter(
    private val onOrderClick: (AdminOrder) -> Unit,
    private val onStatusClick: (AdminOrder) -> Unit
) : ListAdapter<AdminOrder, AdminOrderAdapter.AdminOrderViewHolder>(AdminOrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderViewHolder {
        val binding = ItemAdminOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminOrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order, onOrderClick, onStatusClick)
    }

    /**
     * ✅ Get current item count for debugging
     */
    fun getCurrentItemCount(): Int = itemCount

    /**
     * ✅ Get last item position
     */
    fun getLastItemPosition(): Int = if (itemCount > 0) itemCount - 1 else -1

    class AdminOrderViewHolder(
        private val binding: ItemAdminOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            order: AdminOrder,
            onOrderClick: (AdminOrder) -> Unit,
            onStatusClick: (AdminOrder) -> Unit
        ) {
            binding.apply {
                // ✅ Basic info
                tvOrderNumber.text = order.orderNumber
                tvCustomerName.text = order.customerName
                tvCustomerPhone.text = order.customerPhone
                tvTotalAmount.text = order.formattedTotalAmount
                tvCreatedAt.text = order.formattedCreatedAt

                // ✅ Status
                tvStatus.text = order.statusDisplayName
                tvStatus.setTextColor(order.getStatusColor())

                // ✅ Payment status
                tvPaymentStatus.text = order.paymentStatusDisplayName
                tvPaymentStatus.setTextColor(order.getPaymentStatusColor())

                // ✅ Payment method
                tvPaymentMethod.text = order.paymentMethod ?: "Chưa chọn"

                // ✅ Click listeners
                root.setOnClickListener { onOrderClick(order) }
                btnChangeStatus.setOnClickListener { onStatusClick(order) }
            }
        }
    }
}

class AdminOrderDiffCallback : DiffUtil.ItemCallback<AdminOrder>() {
    override fun areItemsTheSame(oldItem: AdminOrder, newItem: AdminOrder): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: AdminOrder, newItem: AdminOrder): Boolean {
        return oldItem == newItem
    }
} 