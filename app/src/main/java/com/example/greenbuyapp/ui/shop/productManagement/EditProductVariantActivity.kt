package com.example.greenbuyapp.ui.shop.productManagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        binding = ActivityEditProductVariantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        if (productId == -1) {
            finish()
            return
        }
        
        // Add EditProductVariantFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditProductVariantFragment.newInstance(productId))
                .commit()
        }
    }
} 