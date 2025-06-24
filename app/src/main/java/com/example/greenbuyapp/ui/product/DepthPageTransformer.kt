package com.example.greenbuyapp.ui.product

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * Depth Page Transformer - tạo hiệu ứng 3D depth khi swipe giữa các trang
 * Hiệu ứng: trang hiện tại scale down và fade out, trang mới xuất hiện từ phía sau
 */
class DepthPageTransformer : ViewPager2.PageTransformer {

    companion object {
        private const val MIN_SCALE = 0.75f
        private const val MIN_ALPHA = 0.5f
    }

    override fun transformPage(view: View, position: Float) {
        val pageWidth = view.width

        when {
            // Trang bên trái (đã qua)
            position < -1 -> {
                view.alpha = 0f
            }
            
            // Trang hiện tại đang slide ra
            position <= 0 -> {
                view.alpha = 1f
                view.translationX = 0f
                view.translationZ = 0f
                view.scaleX = 1f
                view.scaleY = 1f
            }
            
            // Trang tiếp theo đang slide vào
            position <= 1 -> {
                // Fade out
                view.alpha = 1 - position

                // Di chuyển sang trái
                view.translationX = pageWidth * -position

                // Scale down
                val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position))
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor

                // Set Z order - trang mới xuất hiện từ phía sau
                view.translationZ = -abs(position)
            }
            
            // Trang bên phải (chưa tới)
            else -> {
                view.alpha = 0f
            }
        }
    }
}

/**
 * Enhanced Depth Page Transformer với hiệu ứng mượt mà hơn
 */
class EnhancedDepthPageTransformer : ViewPager2.PageTransformer {

    companion object {
        private const val MIN_SCALE = 0.8f
        private const val SCALE_FACTOR = 0.85f
        private const val MIN_ALPHA = 0.4f
    }

    override fun transformPage(view: View, position: Float) {
        when {
            position < -1 -> {
                // Trang hoàn toàn bên trái
                view.alpha = 0f
                view.scaleX = MIN_SCALE
                view.scaleY = MIN_SCALE
            }
            
            position <= 0 -> {
                // Trang hiện tại (0 to -1)
                view.alpha = 1f
                view.scaleX = 1f
                view.scaleY = 1f
                view.translationX = 0f
                view.translationZ = 1f
            }
            
            position <= 1 -> {
                // Trang đang vào (0 to 1)
                val absPosition = abs(position)
                
                // Alpha transition
                view.alpha = 1 - absPosition * (1 - MIN_ALPHA)
                
                // Scale transition
                val scale = SCALE_FACTOR + (1 - SCALE_FACTOR) * (1 - absPosition)
                view.scaleX = scale
                view.scaleY = scale
                
                // Translation để tạo hiệu ứng depth
                view.translationX = -position * view.width * 0.3f
                
                // Z-order: trang mới ở phía sau
                view.translationZ = -absPosition
                
                // Rotation để tạo hiệu ứng 3D
                view.rotationY = position * -15f
                view.pivotX = if (position < 0) view.width.toFloat() else 0f
                view.pivotY = view.height * 0.5f
            }
            
            else -> {
                // Trang hoàn toàn bên phải
                view.alpha = 0f
                view.scaleX = MIN_SCALE
                view.scaleY = MIN_SCALE
            }
        }
    }
}

/**
 * Cube Page Transformer - hiệu ứng xoay như cube 3D
 */
class CubePageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        when {
            position < -1 -> {
                view.alpha = 0f
            }
            
            position <= 0 -> {
                view.alpha = 1f
                view.pivotX = view.width.toFloat()
                view.rotationY = 90 * abs(position)
            }
            
            position <= 1 -> {
                view.alpha = 1f
                view.pivotX = 0f
                view.rotationY = -90 * abs(position)
            }
            
            else -> {
                view.alpha = 0f
            }
        }
    }
}

/**
 * Hero Card Page Transformer - giống hình ảnh user cung cấp
 * Hiệu ứng: trang chính ở giữa scale full, các trang bên scale nhỏ và fade
 */
class HeroCardPageTransformer : ViewPager2.PageTransformer {

    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.6f
        private const val SCALE_FACTOR = 0.15f
    }

    override fun transformPage(view: View, position: Float) {
        val absPosition = abs(position)
        
        when {
            position < -1 || position > 1 -> {
                // Trang ngoài viewport
                view.alpha = 0f
                view.scaleX = MIN_SCALE
                view.scaleY = MIN_SCALE
            }
            
            else -> {
                // Trang trong viewport
                if (position == 0f) {
                    // Trang chính giữa - full scale
                    view.alpha = 1f
                    view.scaleX = 1f
                    view.scaleY = 1f
                    view.translationZ = 1f
                } else {
                    // Trang bên cạnh - scale nhỏ và fade
                    val scale = MIN_SCALE + (1 - MIN_SCALE) * (1 - absPosition)
                    val alpha = MIN_ALPHA + (1 - MIN_ALPHA) * (1 - absPosition)
                    
                    view.scaleX = scale
                    view.scaleY = scale
                    view.alpha = alpha
                    view.translationZ = -absPosition
                    
                    // Thêm hiệu ứng parallax nhẹ
                    view.translationX = -position * view.width * 0.1f
                }
            }
        }
    }
} 