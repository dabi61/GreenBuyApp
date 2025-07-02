package com.example.greenbuyapp.ui.shop.addProduct

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.greenbuyapp.R
import com.example.greenbuyapp.data.category.model.SubCategory

class SubCategoryAdapter(
    context: Context,
    private val subCategories: List<SubCategory>
) : ArrayAdapter<SubCategory>(context, R.layout.item_dropdown_subcategory, subCategories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_dropdown_subcategory, parent, false
        )
        
        val subCategory = getItem(position)
        val textView = view.findViewById<TextView>(R.id.tv_subcategory_name)
        
        subCategory?.let {
            textView.text = it.name
        }
        
        return view
    }
    
    override fun getCount(): Int = subCategories.size
    
    override fun getItem(position: Int): SubCategory? {
        return if (position >= 0 && position < subCategories.size) {
            subCategories[position]
        } else {
            null
        }
    }

//    override fun getFilter() = null // Disable filtering
} 