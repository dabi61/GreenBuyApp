//package com.example.greenbuyapp.ui.base
//
//import android.os.Bundle
//import androidx.activity.viewModels
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.greenbuyapp.R
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//
///**
// * Ví dụ về cách sử dụng BaseActivity
// *
// * Lưu ý: Đây chỉ là ví dụ minh họa, bạn có thể xóa file này sau khi đã hiểu cách sử dụng
// */
//class ExampleActivity : BaseActivity<ActivityExampleBinding>(R.layout.activity_example) {
//
//    // Cách 1: Sử dụng viewModels() delegate từ activity-ktx
//    override val viewModel: ExampleActivityViewModel by viewModels()
//
//    // Cách 2: Sử dụng Koin injection (nếu đã setup Koin)
//    // override val viewModel: ExampleActivityViewModel by viewModel()
//
//    // ViewBinding instance
//    override val binding: ActivityExampleBinding by lazy {
//        ActivityExampleBinding.inflate(layoutInflater)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        // Phải gọi setContentView trước khi gọi super.onCreate
//        // vì super.onCreate sẽ sử dụng binding.root
//        setContentView(binding.root)
//
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun initViews() {
//        // Setup views, click listeners, etc.
//        with(binding) {
//            buttonExample.setOnClickListener {
//                (viewModel as ExampleActivityViewModel).onButtonClick()
//            }
//
//            buttonShowError.setOnClickListener {
//                showError("Đây là thông báo lỗi ví dụ")
//            }
//
//            buttonNavigate.setOnClickListener {
//                navigateTo(ExampleFragment.newInstance())
//            }
//        }
//
//        // Setup toolbar
//        setSupportActionBar(binding.toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.title = "Example Activity"
//    }
//
//    override fun observeViewModel() {
//        // Observe StateFlow từ ViewModel
//        launchWhenStarted {
//            (viewModel as ExampleActivityViewModel).uiState.collect { state ->
//                updateUI(state)
//            }
//        }
//
//        // Observe loading state
//        launchWhenStarted {
//            (viewModel as ExampleActivityViewModel).isLoading.collect { isLoading ->
//                if (isLoading) {
//                    showLoading()
//                } else {
//                    hideLoading()
//                }
//            }
//        }
//    }
//
//    private fun updateUI(state: ExampleActivityUiState) {
//        with(binding) {
//            textViewExample.text = state.text
//            buttonExample.isEnabled = !state.isLoading
//        }
//    }
//}
//
//// Example ViewModel
//class ExampleActivityViewModel : ViewModel() {
//
//    private val _uiState = MutableStateFlow(ExampleActivityUiState())
//    val uiState: StateFlow<ExampleActivityUiState> = _uiState.asStateFlow()
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
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
//data class ExampleActivityUiState(
//    val text: String = "Initial text",
//    val isLoading: Boolean = false
//)