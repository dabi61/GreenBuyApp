//package com.example.greenbuyapp.ui.base
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.greenbuyapp.R
//import com.example.greenbuyapp.databinding.FragmentExampleBinding
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//
///**
// * Ví dụ về cách sử dụng BaseFragment
// *
// * Lưu ý: Đây chỉ là ví dụ minh họa, bạn có thể xóa file này sau khi đã hiểu cách sử dụng
// */
//class ExampleFragment : BaseFragment<FragmentExampleBinding, ExampleViewModel>() {
//
//    // Cách 1: Sử dụng viewModels() delegate từ fragment-ktx
//    override val viewModel: ExampleViewModel by viewModels()
//
//    // Cách 2: Sử dụng Koin injection (nếu đã setup Koin)
//    // override val viewModel: ExampleViewModel by viewModel()
//
//    override fun getLayoutResourceId(): Int = R.layout.fragment_example
//
//    override fun createViewBinding(
//        inflater: LayoutInflater,
//        container: ViewGroup?
//    ): FragmentExampleBinding {
//        return FragmentExampleBinding.inflate(inflater, container, false)
//    }
//
//    override fun initView() {
//        // Setup views, click listeners, etc.
//        with(binding) {
//            buttonExample.setOnClickListener {
//                viewModel.onButtonClick()
//            }
//
//            // Ví dụ sử dụng các utility methods từ BaseFragment
//            buttonNavigate.setOnClickListener {
//                // Navigate to another fragment
//                navigateTo(AnotherFragment())
//            }
//
//            buttonShowError.setOnClickListener {
//                showError("This is an error message")
//            }
//        }
//    }
//
//    override fun observeViewModel() {
//        // Observe LiveData/StateFlow từ ViewModel
//        launchWhenStarted {
//            viewModel.uiState.collect { state ->
//                updateUI(state)
//            }
//        }
//
//        // Hoặc sử dụng LiveData
//        viewModel.textLiveData.observe(viewLifecycleOwner) { text ->
//            binding.textViewExample.text = text
//        }
//
//        // Observe loading state
//        launchWhenStarted {
//            viewModel.isLoading.collect { isLoading ->
//                if (isLoading) {
//                    showLoading()
//                } else {
//                    hideLoading()
//                }
//            }
//        }
//    }
//
//    private fun updateUI(state: ExampleUiState) {
//        with(binding) {
//            textViewExample.text = state.text
//            buttonExample.isEnabled = !state.isLoading
//        }
//    }
//
//    companion object {
//        fun newInstance(): ExampleFragment {
//            return ExampleFragment().apply {
//                arguments = Bundle().apply {
//                    // Put arguments here if needed
//                }
//            }
//        }
//    }
//}
//
//// Example ViewModel
//class ExampleViewModel : ViewModel() {
//
//    private val _uiState = MutableStateFlow(ExampleUiState())
//    val uiState: StateFlow<ExampleUiState> = _uiState.asStateFlow()
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
//
//    val textLiveData = MutableLiveData<String>()
//
//    init {
//        textLiveData.value = "Hello from ViewModel"
//    }
//
//    fun onButtonClick() {
//        // Handle button click
//        _isLoading.value = true
//
//        viewModelScope.launch {
//            // Simulate network call
//            delay(2000)
//
//            _uiState.update { currentState ->
//                currentState.copy(
//                    text = "Button clicked!",
//                    isLoading = false
//                )
//            }
//
//            _isLoading.value = false
//        }
//    }
//}
//
//// UI State data class
//data class ExampleUiState(
//    val text: String = "Initial text",
//    val isLoading: Boolean = false
//)
//
//// Another fragment for navigation example
//class AnotherFragment : BaseFragment<FragmentExampleBinding, ExampleViewModel>() {
//    override val viewModel: ExampleViewModel by viewModels()
//
//    override fun getLayoutResourceId(): Int = R.layout.fragment_example
//
//    override fun createViewBinding(
//        inflater: LayoutInflater,
//        container: ViewGroup?
//    ): FragmentExampleBinding {
//        return FragmentExampleBinding.inflate(inflater, container, false)
//    }
//
//    override fun initView() {
//        binding.textViewExample.text = "Another Fragment"
//    }
//
//    override fun observeViewModel() {
//        // Empty for this example
//    }
//}