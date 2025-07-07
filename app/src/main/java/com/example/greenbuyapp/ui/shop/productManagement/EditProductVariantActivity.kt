package com.example.greenbuyapp.ui.shop.productManagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityEditProductVariantBinding

class EditProductVariantActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityEditProductVariantBinding
    
    companion object {
        private const val EXTRA_PRODUCT_ID = "extra_product_id"
        
        fun createIntent(context: Context, productId: Int): Intent {
            return Intent(context, EditProductVariantActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT_ID, productId)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProductVariantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemInsets.top, 0, systemInsets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        
        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        if (productId == -1) {
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener {
            val intent = ProductManagementActivity.createIntent(this)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish() // Optional: đóng Activity C nếu cần
        }
        
        // Add EditProductVariantFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditProductVariantFragment.newInstance(productId))
                .commit()
        }
    }
} 