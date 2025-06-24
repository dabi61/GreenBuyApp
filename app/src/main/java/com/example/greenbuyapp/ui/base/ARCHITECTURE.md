# Kiến trúc lớp cơ sở trong GreenBuyApp

Tài liệu này mô tả chi tiết về kiến trúc và tác dụng của các lớp cơ sở `BaseActivity` và `BaseFragment` trong ứng dụng GreenBuyApp.

## Mục lục

- [Giới thiệu](#giới-thiệu)
- [BaseActivity](#baseactivity)
  - [Tính năng chính](#tính-năng-chính-của-baseactivity)
  - [Cách triển khai](#cách-triển-khai-baseactivity)
  - [Lợi ích](#lợi-ích-của-baseactivity)
- [BaseFragment](#basefragment)
  - [Tính năng chính](#tính-năng-chính-của-basefragment)
  - [Cách triển khai](#cách-triển-khai-basefragment)
  - [Lợi ích](#lợi-ích-của-basefragment)
- [Mối quan hệ giữa BaseActivity và BaseFragment](#mối-quan-hệ-giữa-baseactivity-và-basefragment)
- [Best Practices](#best-practices)
- [Kết luận](#kết-luận)

## Giới thiệu

Trong phát triển ứng dụng Android, việc thiết kế các lớp cơ sở (Base Classes) là một phần quan trọng của kiến trúc ứng dụng. GreenBuyApp sử dụng hai lớp cơ sở chính là `BaseActivity` và `BaseFragment` để cung cấp một nền tảng nhất quán cho toàn bộ ứng dụng.

## BaseActivity

`BaseActivity` là lớp cơ sở cho tất cả các Activity trong ứng dụng, cung cấp các tính năng chung và đảm bảo tính nhất quán trong trải nghiệm người dùng.

### Tính năng chính của BaseActivity

1. **ViewBinding tích hợp**
   - Sử dụng generic type để đảm bảo type safety
   - Truy cập các thành phần UI một cách an toàn và hiệu quả

2. **Quản lý vòng đời**
   - Chuẩn hóa các phương thức vòng đời
   - Tự động xử lý các tác vụ chung trong `onCreate()`

3. **Edge-to-edge UI**
   - Thiết lập giao diện edge-to-edge hiện đại
   - Xử lý insets một cách chuyên nghiệp

4. **Xử lý back navigation thông minh**
   - Tùy chỉnh hành vi nút Back
   - Điều hướng về MainActivity khi cần thiết

5. **Xử lý token hết hạn**
   - Tự động phát hiện và xử lý token hết hạn
   - Hiển thị dialog và điều hướng đến màn hình đăng nhập

6. **Hỗ trợ đa ngôn ngữ**
   - Tự động áp dụng ngôn ngữ từ SharedPreferences
   - Đảm bảo ngôn ngữ nhất quán trong toàn bộ ứng dụng

7. **Các tiện ích UI chung**
   - `showLoading()` / `hideLoading()`
   - `showError()`
   - `navigateTo()`

8. **Hỗ trợ Coroutines**
   - `launchWhenStarted()` / `launchWhenResumed()`
   - Quản lý coroutines theo lifecycle

### Cách triển khai BaseActivity

```kotlin
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity {
    // Constructors
    constructor() : super()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    // Abstract properties
    abstract val viewModel: ViewModel?
    abstract val binding: VB

    // Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup common features
        setRecentAppsHeaderColor()
        applyLanguage(sharedPreferencesRepository.locale)
        setupEdgeToEdge()
        setupBackNavigation()
        // ...
    }

    // Template methods for subclasses
    protected open fun initViews() { /* Override in subclasses */ }
    protected open fun observeViewModel() { /* Override in subclasses */ }

    // Utility methods
    protected fun showLoading() { /* ... */ }
    protected fun hideLoading() { /* ... */ }
    protected fun showError(message: String) { /* ... */ }
    protected fun navigateTo(fragment: Fragment, ...) { /* ... */ }
    // ...
}
```

### Lợi ích của BaseActivity

1. **Giảm code trùng lặp**
   - Các tính năng chung được triển khai một lần
   - Không phải viết lại code xử lý token, back navigation, v.v.

2. **Tính nhất quán**
   - Đảm bảo tất cả các Activity có cùng hành vi cơ bản
   - Trải nghiệm người dùng nhất quán

3. **Dễ mở rộng**
   - Thêm tính năng mới vào BaseActivity sẽ áp dụng cho tất cả các Activity
   - Dễ dàng thêm các tính năng mới trong tương lai

4. **Tách biệt mối quan tâm**
   - Mỗi phương thức có một trách nhiệm rõ ràng
   - Dễ dàng đọc hiểu và bảo trì

## BaseFragment

`BaseFragment` là lớp cơ sở cho tất cả các Fragment trong ứng dụng, cung cấp các tính năng chung và đảm bảo quản lý vòng đời an toàn.

### Tính năng chính của BaseFragment

1. **ViewBinding với Null Safety**
   - Quản lý ViewBinding an toàn với null check
   - Tự động clear binding khi Fragment bị destroy

2. **Tích hợp ViewModel**
   - Bắt buộc các Fragment con phải có ViewModel
   - Tuân thủ kiến trúc MVVM

3. **Quản lý vòng đời an toàn**
   - Xử lý các sự kiện vòng đời một cách chuẩn hóa
   - Tránh memory leak

4. **Tham chiếu đến BaseActivity**
   - Truy cập các tiện ích từ BaseActivity
   - Tự động clear tham chiếu khi Fragment bị detach

5. **Cấu trúc chuẩn hóa**
   - Các phương thức trừu tượng định nghĩa cấu trúc chuẩn
   - Tách biệt khởi tạo UI và observe data

6. **Các tiện ích UI chung**
   - `showLoading()` / `hideLoading()`
   - `showError()`
   - `navigateTo()`

7. **Hỗ trợ Coroutines**
   - `launchWhenStarted()` / `launchWhenResumed()`
   - Quản lý coroutines theo lifecycle

### Cách triển khai BaseFragment

```kotlin
abstract class BaseFragment<VB : ViewBinding, VM : ViewModel> : Fragment() {
    // ViewBinding with null safety
    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException("ViewBinding is not initialized")

    // Abstract properties and methods
    protected abstract val viewModel: VM
    protected abstract fun getLayoutResourceId(): Int
    protected abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    protected abstract fun initView()
    protected abstract fun observeViewModel()

    // Lifecycle methods
    override fun onCreateView(...): View? {
        _binding = createViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(...) {
        // Initialize views and observe data
        initView()
        observeViewModel()
        // ...
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Avoid memory leak
    }

    // Utility methods
    protected fun showLoading() { /* ... */ }
    protected fun hideLoading() { /* ... */ }
    protected fun showError(message: String) { /* ... */ }
    protected fun navigateTo(fragment: Fragment, ...) { /* ... */ }
    // ...
}
```

### Lợi ích của BaseFragment

1. **Quản lý vòng đời an toàn**
   - Tránh memory leak với ViewBinding
   - Clear tham chiếu khi không cần thiết

2. **Tính nhất quán**
   - Cấu trúc chuẩn hóa với các phương thức trừu tượng
   - Hành vi nhất quán trong toàn bộ ứng dụng

3. **Tách biệt mối quan tâm**
   - `initView()` cho khởi tạo UI
   - `observeViewModel()` cho việc observe data

4. **Dễ mở rộng**
   - Thêm tính năng mới vào BaseFragment sẽ áp dụng cho tất cả các Fragment
   - Dễ dàng thêm các tính năng mới trong tương lai

## Mối quan hệ giữa BaseActivity và BaseFragment

1. **Tương tác hai chiều**
   - BaseFragment có tham chiếu đến BaseActivity
   - BaseActivity cung cấp phương thức để điều hướng giữa các Fragment

2. **Nhất quán trong xử lý sự kiện**
   - Cả hai đều observe token expired events
   - Cả hai đều xử lý back navigation một cách nhất quán

3. **Phân chia trách nhiệm**
   - BaseActivity xử lý các tác vụ cấp Activity
   - BaseFragment xử lý các tác vụ cấp Fragment

## Best Practices

1. **Không lạm dụng BaseActivity/BaseFragment**
   - Chỉ đưa các tính năng thực sự chung vào các lớp cơ sở
   - Tránh làm cho các lớp cơ sở trở nên quá phức tạp

2. **Sử dụng các phương thức template**
   - Định nghĩa các phương thức trừu tượng để đảm bảo cấu trúc chuẩn
   - Sử dụng các phương thức `open` để cho phép override khi cần

3. **Quản lý vòng đời cẩn thận**
   - Clear tham chiếu khi không cần thiết
   - Sử dụng coroutines với lifecycle awareness

4. **Tách biệt mối quan tâm**
   - Mỗi phương thức có một trách nhiệm rõ ràng
   - Tách biệt khởi tạo UI và observe data

## Kết luận

BaseActivity và BaseFragment là hai lớp cơ sở quan trọng trong kiến trúc của GreenBuyApp. Chúng cung cấp một nền tảng vững chắc cho tất cả các Activity và Fragment trong ứng dụng, đảm bảo tính nhất quán, tái sử dụng code và dễ bảo trì.

Thiết kế này tuân thủ các nguyên tắc phát triển phần mềm hiện đại như tách biệt mối quan tâm, tái sử dụng code và sử dụng các API mới nhất của Android. Việc đầu tư thời gian vào việc thiết kế các lớp cơ sở tốt sẽ mang lại lợi ích lâu dài cho dự án, giúp giảm thời gian phát triển, tăng chất lượng code và cải thiện trải nghiệm người dùng. 