package com.example.greenbuyapp.ui.notification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.FragmentNotificationBinding

/**
 * Fragment hiển thị màn hình thông báo
 */
class NotificationFragment : Fragment() {
    
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

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
    }
    
    private fun initViews() {
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}