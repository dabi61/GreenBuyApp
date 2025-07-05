package com.example.greenbuyapp.ui.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.notice.model.Notice

class PendingNoticeAdapter : RecyclerView.Adapter<PendingNoticeAdapter.NoticeViewHolder>() {

    private var notices = listOf<Notice>()

    fun submitList(newList: List<Notice>) {
        notices = newList
        notifyDataSetChanged()
    }

    inner class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(notice: Notice) {
            itemView.findViewById<TextView>(R.id.tvOrderNumber).text = notice.order_number
            itemView.findViewById<TextView>(R.id.tvCreatedAt).text = notice.created_at
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
