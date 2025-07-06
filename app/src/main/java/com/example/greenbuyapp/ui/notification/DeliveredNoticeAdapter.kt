package com.example.greenbuyapp.ui.notification

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.notice.model.Notice

class DeliveredNoticeAdapter : RecyclerView.Adapter<DeliveredNoticeAdapter.NoticeViewHolder>() {

    private var notices = listOf<Notice>()
    var onItemClick: ((Notice) -> Unit)? = null

    fun submitList(newList: List<Notice>) {
        notices = newList
        notifyDataSetChanged()
    }

    inner class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(notice: Notice) {
            val orderNumber = notice.order_number ?: "??"

            val fullText = "Đơn hàng $orderNumber đã giao thành công đến bạn, hãy đánh giá sản phẩm nhé."
            val spannable = SpannableString(fullText)

            val startIndex = fullText.indexOf(orderNumber)
            val endIndex = startIndex + orderNumber.length

            if (startIndex >= 0) {
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#00AA00")),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            itemView.findViewById<TextView>(R.id.tvDeliveryStatus).text = spannable

            itemView.setOnClickListener {
                onItemClick?.invoke(notice)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_notice, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(notices[position])
    }

    override fun getItemCount(): Int = notices.size
}
