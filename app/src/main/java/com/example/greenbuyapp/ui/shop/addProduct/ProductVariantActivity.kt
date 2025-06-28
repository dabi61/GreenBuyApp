package com.example.greenbuyapp.ui.shop.addProduct

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.greenbuyapp.R
import com.example.greenbuyapp.databinding.ActivityProductVariantBinding

class ProductVariantActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProductVariantBinding
    
    companion object {
        private const val EXTRA_PRODUCT_ID = "extra_product_id"
        
        fun createIntent(context: Context, productId: Int): Intent {
            return Intent(context, ProductVariantActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT_ID, productId)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductVariantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        if (productId == -1) {
            finish()
            return
        }
        
        // Add ProductVariantFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProductVariantFragment.newInstance(productId))
                .commit()
        }
    }
} 