package com.example.greenbuyapp.ui.base

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.example.greenbuyapp.R
import com.example.greenbuyapp.domain.login.TokenExpiredManager
import com.example.greenbuyapp.ui.login.LoginActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Base Fragment với ViewBinding và ViewModel hiện đại
 * @param VB ViewBinding type
 * @param VM ViewModel type
 */
abstract class BaseFragment<VB : ViewBinding, VM : ViewModel> : Fragment() {

    // ViewBinding instance - sẽ tự động clear khi Fragment destroy view
    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException("ViewBinding is not initialized")

    // ViewModel instance
    protected abstract val viewModel: VM

    // BaseActivity reference
    protected var baseActivity: BaseActivity<*>? = null
        private set

    // Token expired manager
    private val tokenExpiredManager: TokenExpiredManager by inject()

    // Layout resource ID
    @LayoutRes
    protected abstract fun getLayoutResourceId(): Int

    // Abstract method để tạo ViewBinding instance
    protected abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    // Initialize views và setup listeners
    protected abstract fun initView()

    // Observe LiveData/Flow từ ViewModel
    protected abstract fun observeViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity<*>) {
            baseActivity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        initView()
        
        // Setup observers
        observeViewModel()
        
        // Observe token expired events nếu không phải trong LoginActivity
        if (activity !is LoginActivity ) {
            observeTokenExpired()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressedDispatcher?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        
        // Log để track fragment lifecycle
        println("🔄 ${this::class.simpleName} onDestroyView called")
        
        _binding = null
    }
    
    /**
     * Check if fragment is in safe state to perform UI operations
     */
    protected fun isFragmentSafe(): Boolean {
        return isAdded && !isRemoving && !isDetached && activity != null && 
               !requireActivity().isFinishing && !requireActivity().isDestroyed
    }

    /**
     * ✅ Check if ViewBinding is initialized
     */
    protected fun isBindingInitialized(): Boolean {
        return _binding != null
    }

    override fun onDetach() {
        super.onDetach()
        baseActivity = null
    }

    /**
     * Observe token expired events
     */
    private fun observeTokenExpired() {
        viewLifecycleOwner.lifecycleScope.launch {
            tokenExpiredManager.tokenExpiredEvent.collect { _ ->
                // BaseActivity sẽ xử lý việc hiển thị dialog và navigate
                // Fragment chỉ cần observe event
            }
        }
    }

    /**
     * Show loading dialog
     */
    protected fun showLoading() {
        baseActivity?.let {
            // Implement loading dialog trong BaseActivity nếu cần
        }
    }

    /**
     * Hide loading dialog
     */
    protected fun hideLoading() {
        baseActivity?.let {
            // Implement hide loading dialog trong BaseActivity nếu cần
        }
    }

    /**
     * Show error message
     */
    protected fun showError(message: String) {
        baseActivity?.let {
            // Implement show error trong BaseActivity nếu cần
        }
    }

    /**
     * Navigate to another fragment (REPLACE current fragment)
     */
    protected fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
        baseActivity?.let { activity ->
            val transaction = activity.supportFragmentManager.beginTransaction()
            
            // Add animation nếu cần
            transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            
            transaction.replace(android.R.id.content, fragment)
            
            if (addToBackStack) {
                transaction.addToBackStack(null)
            }
            
            transaction.commit()
        }
    }

    /**
     * Add fragment on top of current fragment (current fragment vẫn tồn tại)
     */
    protected fun addFragment(
        fragment: Fragment, 
        containerId: Int = android.R.id.content,
        addToBackStack: Boolean = true,
        tag: String? = null
    ) {
        baseActivity?.let { activity ->
            val transaction = activity.supportFragmentManager.beginTransaction()
            
            // Add slide animation cho add fragment
            transaction.setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            
            // ADD fragment (không thay thế)
            transaction.add(containerId, fragment, tag)
            
            if (addToBackStack) {
                transaction.addToBackStack(tag)
            }
            
            transaction.commit()
        }
    }

    /**
     * Show/Hide fragments để quản lý multiple fragments đã được add
     */
    protected fun showFragment(fragmentToShow: Fragment, fragmentToHide: Fragment? = null) {
        baseActivity?.let { activity ->
            val transaction = activity.supportFragmentManager.beginTransaction()
            
            transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            
            // Show fragment cần hiển thị
            transaction.show(fragmentToShow)
            
            // Hide fragment cần ẩn (nếu có)
            fragmentToHide?.let {
                transaction.hide(it)
            }
            
            transaction.commit()
        }
    }

    /**
     * Remove fragment khỏi back stack
     */
    protected fun removeFragment(fragment: Fragment) {
        baseActivity?.let { activity ->
            val transaction = activity.supportFragmentManager.beginTransaction()
            
            transaction.setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            
            transaction.remove(fragment)
            transaction.commit()
        }
    }

    // Note: Full screen fragment methods removed since we now use Activities for complex screens

    

    /**
     * Convenience method để launch coroutine trong fragment lifecycle scope
     */
    protected fun launchWhenStarted(block: suspend () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            block()
        }
    }

    /**
     * Convenience method để launch coroutine trong fragment lifecycle scope
     */
    protected fun launchWhenResumed(block: suspend () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            block()
        }
    }



}