# Hướng dẫn sử dụng BaseActivity và BaseFragment

Tài liệu này hướng dẫn cách sử dụng các lớp cơ sở `BaseActivity` và `BaseFragment` trong ứng dụng GreenBuyApp.

## Mục lục
- [Giới thiệu](#giới-thiệu)
- [BaseActivity](#baseactivity)
  - [Tính năng chính](#tính-năng-chính-của-baseactivity)
  - [Cách sử dụng](#cách-sử-dụng-baseactivity)
  - [Ví dụ](#ví-dụ-baseactivity)
- [BaseFragment](#basefragment)
  - [Tính năng chính](#tính-năng-chính-của-basefragment)
  - [Cách sử dụng](#cách-sử-dụng-basefragment)
  - [Ví dụ](#ví-dụ-basefragment)
- [Các tiện ích chung](#các-tiện-ích-chung)

## Giới thiệu

Các lớp cơ sở `BaseActivity` và `BaseFragment` được thiết kế để cung cấp một nền tảng nhất quán cho tất cả các Activity và Fragment trong ứng dụng. Chúng tích hợp các tính năng hiện đại của Android như ViewBinding, ViewModel, Coroutines, và StateFlow.

## BaseActivity

### Tính năng chính của BaseActivity

- **ViewBinding**: Tích hợp ViewBinding để truy cập an toàn vào các thành phần UI
- **ViewModel**: Hỗ trợ MVVM pattern với ViewModel
- **Edge-to-edge UI**: Tự động thiết lập giao diện edge-to-edge
- **Xử lý back navigation**: Quản lý back navigation thông minh
- **Token expired handling**: Tự động xử lý token hết hạn
- **Loading state management**: Quản lý trạng thái loading
- **Error handling**: Hiển thị thông báo lỗi
- **Fragment navigation**: Điều hướng giữa các Fragment
- **Coroutines support**: Hỗ trợ Coroutines với lifecycle awareness

### Cách sử dụng BaseActivity

1. **Khai báo lớp con**:
   ```kotlin
   class MyActivity : BaseActivity<ActivityMyBinding>(R.layout.activity_my) {
       // Implement các phương thức bắt buộc
   }
   ```

2. **Implement các thành phần bắt buộc**:
   ```kotlin
   // Khai báo ViewModel
   override val viewModel: MyViewModel by viewModels()
   
   // Khởi tạo ViewBinding
   override val binding: ActivityMyBinding by lazy {
       ActivityMyBinding.inflate(layoutInflater)
   }
   ```

3. **Override onCreate**:
   ```kotlin
   override fun onCreate(savedInstanceState: Bundle?) {
       // Quan trọng: Phải gọi setContentView trước super.onCreate
       setContentView(binding.root)
       super.onCreate(savedInstanceState)
   }
   ```

4. **Khởi tạo views**:
   ```kotlin
   override fun initViews() {
       // Thiết lập views, click listeners, etc.
       binding.button.setOnClickListener { ... }
       
       // Thiết lập toolbar nếu cần
       setSupportActionBar(binding.toolbar)
       supportActionBar?.setDisplayHomeAsUpEnabled(true)
   }
   ```

5. **Observe ViewModel**:
   ```kotlin
   override fun observeViewModel() {
       launchWhenStarted {
           (viewModel as MyViewModel).uiState.collect { state ->
               // Cập nhật UI dựa trên state
           }
       }
   }
   ```

### Ví dụ BaseActivity

Xem file `ExampleActivity.kt` để biết ví dụ đầy đủ về cách sử dụng BaseActivity.

## BaseFragment

### Tính năng chính của BaseFragment

- **ViewBinding**: Tích hợp ViewBinding với tự động cleanup
- **ViewModel**: Hỗ trợ MVVM pattern với ViewModel
- **Lifecycle-aware**: Tự động cleanup resources khi Fragment bị destroy
- **Token expired handling**: Tự động xử lý token hết hạn
- **Loading state management**: Quản lý trạng thái loading
- **Error handling**: Hiển thị thông báo lỗi
- **Fragment navigation**: Điều hướng giữa các Fragment
- **Coroutines support**: Hỗ trợ Coroutines với lifecycle awareness

### Cách sử dụng BaseFragment

1. **Khai báo lớp con**:
   ```kotlin
   class MyFragment : BaseFragment<FragmentMyBinding, MyViewModel>() {
       // Implement các phương thức bắt buộc
   }
   ```

2. **Implement các thành phần bắt buộc**:
   ```kotlin
   // Khai báo ViewModel
   override val viewModel: MyViewModel by viewModels()
   
   // Cung cấp layout resource ID
   override fun getLayoutResourceId(): Int = R.layout.fragment_my
   
   // Khởi tạo ViewBinding
   override fun createViewBinding(
       inflater: LayoutInflater,
       container: ViewGroup?
   ): FragmentMyBinding {
       return FragmentMyBinding.inflate(inflater, container, false)
   }
   ```

3. **Khởi tạo views**:
   ```kotlin
   override fun initView() {
       // Thiết lập views, click listeners, etc.
       binding.button.setOnClickListener { ... }
   }
   ```

4. **Observe ViewModel**:
   ```kotlin
   override fun observeViewModel() {
       launchWhenStarted {
           viewModel.uiState.collect { state ->
               // Cập nhật UI dựa trên state
           }
       }
   }
   ```

5. **Factory method (tùy chọn)**:
   ```kotlin
   companion object {
       fun newInstance(): MyFragment {
           return MyFragment().apply {
               arguments = Bundle().apply {
                   // Đặt arguments nếu cần
               }
           }
       }
   }
   ```

### Ví dụ BaseFragment

Xem file `ExampleFragment.kt` để biết ví dụ đầy đủ về cách sử dụng BaseFragment.

## Các tiện ích chung

Cả `BaseActivity` và `BaseFragment` đều cung cấp các tiện ích sau:

### 1. Hiển thị/ẩn loading

```kotlin
// Hiển thị loading
showLoading()

// Ẩn loading
hideLoading()
```

### 2. Hiển thị thông báo lỗi

```kotlin
showError("Thông báo lỗi")
```

### 3. Điều hướng đến Fragment

```kotlin
// Từ Activity
navigateTo(MyFragment.newInstance())

// Từ Fragment
navigateTo(AnotherFragment.newInstance())
```

### 4. Coroutines với lifecycle awareness

```kotlin
launchWhenStarted {
    // Code sẽ chỉ chạy khi lifecycle ở trạng thái STARTED trở lên
}

launchWhenResumed {
    // Code sẽ chỉ chạy khi lifecycle ở trạng thái RESUMED
}
```

### 5. Xử lý token hết hạn

Token hết hạn sẽ được tự động xử lý, hiển thị dialog và điều hướng đến màn hình đăng nhập khi cần.

---

## Lưu ý quan trọng

1. **ViewBinding cleanup**: BaseFragment tự động clear ViewBinding trong `onDestroyView()` để tránh memory leak.

2. **ViewModel lifecycle**: ViewModel được quản lý theo lifecycle của Activity/Fragment.

3. **Back navigation**: BaseActivity tự động xử lý back navigation, bao gồm cả việc điều hướng về MainActivity khi cần.

4. **Edge-to-edge UI**: BaseActivity tự động thiết lập giao diện edge-to-edge với insets phù hợp.

5. **Locale handling**: BaseActivity tự động áp dụng ngôn ngữ từ SharedPreferences.

---

Để biết thêm chi tiết, vui lòng tham khảo mã nguồn của `BaseActivity.kt` và `BaseFragment.kt`. 