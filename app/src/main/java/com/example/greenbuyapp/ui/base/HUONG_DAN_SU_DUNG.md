# Hướng dẫn sử dụng BaseActivity và BaseFragment

## BaseActivity

### Cách sử dụng

```kotlin
class TenActivity : BaseActivity<TenActivityBinding>(R.layout.ten_activity) {
    
    // Khai báo ViewModel
    override val viewModel: TenViewModel by viewModels()
    
    // Khởi tạo ViewBinding
    override val binding: TenActivityBinding by lazy {
        TenActivityBinding.inflate(layoutInflater)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Quan trọng: Phải gọi setContentView trước super.onCreate
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }
    
    // Khởi tạo views
    override fun initViews() {
        // Thiết lập views, click listeners, etc.
        binding.nutBam.setOnClickListener {
            (viewModel as TenViewModel).xuLySuKien()
        }
    }
    
    // Theo dõi ViewModel
    override fun observeViewModel() {
        launchWhenStarted {
            (viewModel as TenViewModel).trangThai.collect { state ->
                // Cập nhật UI
            }
        }
    }
}
```

### Các tiện ích có sẵn

- `showLoading()` / `hideLoading()`: Hiển thị/ẩn loading
- `showError("Nội dung lỗi")`: Hiển thị thông báo lỗi
- `navigateTo(TenFragment.newInstance())`: Điều hướng đến Fragment
- `launchWhenStarted { }`: Chạy coroutine theo lifecycle

## BaseFragment

### Cách sử dụng

```kotlin
class TenFragment : BaseFragment<FragmentTenBinding, TenViewModel>() {
    
    // Khai báo ViewModel
    override val viewModel: TenViewModel by viewModels()
    
    // Cung cấp layout resource ID
    override fun getLayoutResourceId(): Int = R.layout.fragment_ten
    
    // Khởi tạo ViewBinding
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTenBinding {
        return FragmentTenBinding.inflate(inflater, container, false)
    }
    
    // Khởi tạo views
    override fun initView() {
        binding.nutBam.setOnClickListener {
            viewModel.xuLySuKien()
        }
    }
    
    // Theo dõi ViewModel
    override fun observeViewModel() {
        launchWhenStarted {
            viewModel.trangThai.collect { state ->
                // Cập nhật UI
            }
        }
    }
    
    // Factory method (tùy chọn)
    companion object {
        fun newInstance(): TenFragment {
            return TenFragment()
        }
    }
}
```

### Các tiện ích có sẵn

- `showLoading()` / `hideLoading()`: Hiển thị/ẩn loading
- `showError("Nội dung lỗi")`: Hiển thị thông báo lỗi
- `navigateTo(FragmentKhac.newInstance())`: Điều hướng đến Fragment khác
- `launchWhenStarted { }`: Chạy coroutine theo lifecycle

## Lưu ý quan trọng

1. BaseFragment tự động xóa ViewBinding khi Fragment bị destroy để tránh memory leak
2. BaseActivity tự động xử lý back navigation và token hết hạn
3. Cả hai lớp đều hỗ trợ edge-to-edge UI và đa ngôn ngữ

Chi tiết đầy đủ xem trong file `README.md` 