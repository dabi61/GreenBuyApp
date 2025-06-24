package com.example.greenbuyapp.ui.mall

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.FragmentMallBinding

/**
 * Fragment hiển thị màn hình mall
 */
class MallFragment : Fragment() {
    
    private var _binding: FragmentMallBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMallBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Khởi tạo các view và thiết lập sự kiện
        initViews()
    }
    
    private fun initViews() {
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.main_color)

    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}