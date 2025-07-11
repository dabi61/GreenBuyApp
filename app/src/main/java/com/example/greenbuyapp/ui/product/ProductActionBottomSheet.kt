package com.example.greenbuyapp.ui.product

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.product.model.ProductAttribute
import com.example.greenbuyapp.databinding.BottomSheetProductActionBinding
import com.example.greenbuyapp.util.ImageTransform
import com.example.greenbuyapp.util.loadUrl
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.NumberFormat
import java.util.Locale

class ProductActionBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetProductActionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var productAttribute: ProductAttribute
    private var actionType: ActionType = ActionType.ADD_TO_CART
    private var quantity: Int = 1
    private var onActionListener: ((ProductAttribute, Int, ActionType) -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null // ✅ Thêm dismiss listener
    
    enum class ActionType {
        ADD_TO_CART,
        BUY_NOW
    }
    
    companion object {
        private const val ARG_PRODUCT_ATTRIBUTE = "product_attribute"
        private const val ARG_ACTION_TYPE = "action_type"
        
        fun newInstance(
            productAttribute: ProductAttribute,
            actionType: ActionType
        ): ProductActionBottomSheet {
            return ProductActionBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PRODUCT_ATTRIBUTE, productAttribute)
                    putSerializable(ARG_ACTION_TYPE, actionType)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productAttribute = it.getParcelable(ARG_PRODUCT_ATTRIBUTE)!!
            actionType = it.getSerializable(ARG_ACTION_TYPE) as ActionType
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { bottomSheet ->
                bottomSheet.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_bottom_sheet)
            }
        }
        return dialog
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetProductActionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupQuantityControls()
        setupActionButtons()
    }
    
    private fun setupUI() {
        binding.apply {
            // Load product image
            val imageUrl = if (!productAttribute.image.isNullOrEmpty()) {
                if (productAttribute.image.startsWith("http")) {
                    productAttribute.image
                } else {
                    "https://www.utt-school.site${productAttribute.image}"
                }
            } else {
                null
            }
            
            ivProductImage.loadUrl(
                imageUrl = imageUrl,
                placeholder = R.drawable.pic_item_product,
                error = R.drawable.pic_item_product,
                transform = ImageTransform.ROUNDED,
                cornerRadius = 8
            )
            
            // Set product info
            tvProductPrice.text = productAttribute.getFormattedPrice()
            tvStockAvailable.text = "Kho: ${productAttribute.quantity} sản phẩm"
            tvProductAttributes.text = "Màu: ${productAttribute.color}, Kích thước: ${productAttribute.size}"
            
            // Set initial quantity
            etQuantity.setText(quantity.toString())
            updateTotalPrice()
            
            // ✅ Setup button based on action type
            setupActionButton()
        }
    }
    
    /**
     * ✅ Setup action button dựa trên action type
     */
    private fun setupActionButton() {
        binding.apply {
            when (actionType) {
                ActionType.ADD_TO_CART -> {
                    btnAction.text = "Thêm vào giỏ hàng"
                    btnAction.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.main_color)
                    btnAction.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
                ActionType.BUY_NOW -> {
                    btnAction.text = "Mua ngay"
                    btnAction.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.red_900)
                    btnAction.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
        }
    }
    
    private fun setupQuantityControls() {
        binding.apply {
            // Minus button
            btnMinus.setOnClickListener {
                if (quantity > 1) {
                    quantity--
                    etQuantity.setText(quantity.toString())
                    updateTotalPrice()
                }
            }
            
            // Plus button
            btnPlus.setOnClickListener {
                if (quantity < productAttribute.quantity) {
                    quantity++
                    etQuantity.setText(quantity.toString())
                    updateTotalPrice()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Số lượng không thể vượt quá ${productAttribute.quantity}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            // Quantity input text change
            etQuantity.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                
                override fun afterTextChanged(s: Editable?) {
                    val input = s.toString()
                    if (input.isNotEmpty() && input.isDigitsOnly()) {
                        val newQuantity = input.toIntOrNull() ?: 1
                        
                        quantity = when {
                            newQuantity < 1 -> {
                                etQuantity.setText("1")
                                etQuantity.setSelection(1)
                                1
                            }
                            newQuantity > productAttribute.quantity -> {
                                etQuantity.setText(productAttribute.quantity.toString())
                                etQuantity.setSelection(productAttribute.quantity.toString().length)
                                Toast.makeText(
                                    requireContext(),
                                    "Số lượng không thể vượt quá ${productAttribute.quantity}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                productAttribute.quantity
                            }
                            else -> newQuantity
                        }
                        
                        updateTotalPrice()
                    } else if (input.isEmpty()) {
                        quantity = 1
                        updateTotalPrice()
                    }
                }
            })
        }
    }
    
    private fun setupActionButtons() {
        binding.apply {
            btnAction.setOnClickListener {
                onActionListener?.invoke(productAttribute, quantity, actionType)
                dismiss()
            }
        }
    }
    
    private fun updateTotalPrice() {
        val totalPrice = productAttribute.price * quantity
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        binding.tvTotalPrice.text = formatter.format(totalPrice)
    }
    
    /**
     * Set listener cho action events
     */
    fun setOnActionListener(listener: (ProductAttribute, Int, ActionType) -> Unit) {
        onActionListener = listener
    }
    
    /**
     * ✅ Set listener cho dismiss events
     */
    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // ✅ Gọi dismiss listener khi view bị destroy
        onDismissListener?.invoke()
    }
} 