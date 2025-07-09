package com.example.greenbuyapp.ui.social.shopReview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.data.social.model.GetRatingShopResponse
import com.example.greenbuyapp.databinding.ItemShopReviewBinding

class ShopReviewAdapter :
    RecyclerView.Adapter<ShopReviewAdapter.ShopReviewViewHolder>() {

    private val items = mutableListOf<GetRatingShopResponse>()

    fun submitList(newList: List<GetRatingShopResponse>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopReviewViewHolder {
        val binding = ItemShopReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ShopReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ShopReviewViewHolder(private val binding: ItemShopReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GetRatingShopResponse) {
            binding.tvReviewerName.text = item.user_username
            binding.tvReviewContent.text = item.comment

            // Set visible số sao theo rating (1–5)
            val stars = listOf(
                binding.ivStar1,
                binding.ivStar2,
                binding.ivStar3,
                binding.ivStar4,
                binding.ivStar5
            )

            for (i in stars.indices) {
                stars[i].visibility = if (i < item.rating) View.VISIBLE else View.GONE
            }

            // Avatar: nếu chưa có ảnh, bạn dùng mặc định
            binding.imgReviewerAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
        }
    }
}
