package com.example.greenbuyapp.ui.notification

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.FragmentNotificationBinding
import com.example.greenbuyapp.ui.main.MainActivity
import com.example.greenbuyapp.ui.order.OrderItemSummaryAdapter
import com.example.greenbuyapp.ui.profile.orders.CustomerOrderDetailActivity
import com.example.greenbuyapp.ui.profile.orders.CustomerOrderFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment hiển thị màn hình thông báo
 */
class NotificationFragment : Fragment() {
    
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationViewModel by viewModel()
    private lateinit var adapter: DeliveredNoticeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Khởi tạo các view và thiết lập sự kiện
        initViews()

        adapter = DeliveredNoticeAdapter()

        adapter.onItemClick = { it ->
            val orderId = it.id
            val intent = CustomerOrderDetailActivity.createIntent(requireContext(), orderId)
            startActivity(intent)

            println("🔍 Navigate to customer order detail for orderId: $orderId")
        }

        binding.recyclerViewPendingNotices.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPendingNotices.adapter = adapter

        viewModel.loadPendingNotices()

        lifecycleScope.launch {
            viewModel.pendingNotices.collect { notices ->
                adapter.submitList(notices)
                binding.tvEmpty.visibility = if (notices.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun initViews() {
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}