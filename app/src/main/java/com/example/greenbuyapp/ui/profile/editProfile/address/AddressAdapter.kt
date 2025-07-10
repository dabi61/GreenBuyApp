package com.example.greenbuyapp.ui.profile.editProfile.address

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.data.user.model.AddressResponse
import com.example.greenbuyapp.databinding.ItemAddressBinding

class AddressAdapter :
    ListAdapter<AddressResponse, AddressAdapter.AddressViewHolder>(AddressDiffCallback()) {

    private var userFullName: String = ""
    var onItemLongClick: ((Int) -> Unit)? = null

    fun submitListWithName(list: List<AddressResponse>, name: String) {
        userFullName = name
        submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(getItem(position), userFullName)
        holder.itemView.setOnClickListener {
            val addressId = getItem(holder.adapterPosition).id
            onItemLongClick?.invoke(addressId)
            true
        }
    }

    inner class AddressViewHolder(private val binding: ItemAddressBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(address: AddressResponse, fullName: String) {
//            binding.tvid.text = address.id.toString()
            binding.tvName.text = fullName
            binding.tvStreet.text = address.street
            binding.tvWard.text = "${address.city}, ${address.state}, ${address.zipcode}"
            binding.tvPhone.text = address.phone

            if(address.is_default) {
                binding.tvDefault.text = "Mặc định"
                binding.tvDefault.visibility = View.VISIBLE
            }else{
                binding.tvDefault.visibility = View.GONE
            }
        }
    }

    class AddressDiffCallback : DiffUtil.ItemCallback<AddressResponse>() {
        override fun areItemsTheSame(oldItem: AddressResponse, newItem: AddressResponse) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AddressResponse, newItem: AddressResponse) =
            oldItem == newItem
    }
}
