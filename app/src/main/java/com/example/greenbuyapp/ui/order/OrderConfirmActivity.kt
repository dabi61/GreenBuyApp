package com.example.greenbuyapp.ui.order

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenbuyapp.data.cart.model.CartItem
import com.example.greenbuyapp.databinding.ActivityOrderConfirmBinding
import com.example.greenbuyapp.ui.order.OrderItemSummaryAdapter

class OrderConfirmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderConfirmBinding
    private lateinit var items: List<CartItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        items = intent.getParcelableArrayListExtra<CartItem>(EXTRA_ITEMS) ?: emptyList()

        setupRecycler()
        setupConfirmButton()
    }

    private fun setupRecycler() {
        val adapter = OrderItemSummaryAdapter()
        binding.recyclerViewSummary.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSummary.adapter = adapter
        adapter.submitList(items)
    }

    private fun setupConfirmButton() {
        binding.btnPlaceOrder.setOnClickListener {
            // TODO: validate inputs and call Order API
            Toast.makeText(this, "Đang gửi đơn hàng...", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val EXTRA_ITEMS = "items"
        fun createIntent(context: Context, items: ArrayList<CartItem>): Intent {
            return Intent(context, OrderConfirmActivity::class.java).apply {
                putParcelableArrayListExtra(EXTRA_ITEMS, items)
            }
        }
    }
} 