package com.example.greenbuyapp.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.greenbuyapp.R
import com.google.android.material.snackbar.Snackbar
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import android.graphics.Bitmap
import com.bumptech.glide.load.engine.DiskCacheStrategy

fun View.showSnackBar(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    anchor: View? = null
) {
    Snackbar.make(this, message, duration)
        .setAnchorView(anchor)
        .show()
}

fun View.showSnackBar(
    @StringRes textId: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    anchor: View? = null
) {
    Snackbar.make(this, textId, duration)
        .setAnchorView(anchor)
        .show()
}

fun View.showSnackBar(
    @StringRes textId: Int,
    duration: Int = Snackbar.LENGTH_SHORT,
    @IdRes anchor: Int
) {
    Snackbar.make(this, textId, duration)
        .setAnchorView(anchor)
        .show()
}

fun View.margin(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.let { leftMargin = it }
        top?.let { topMargin = it }
        right?.let { rightMargin = it }
        bottom?.let { bottomMargin = it }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
} // Dùng để custom view

fun View.focusAndShowKeyboard() {
    fun View.showTheKeyboardNow() {
        if (isFocused) {
            post { // Đảm bảo rằng yêu cầu hiển thị bàn phím được thực thi khi view sẵn sàng
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager // Service
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    requestFocus()
    if (hasWindowFocus()) {
        showTheKeyboardNow()
    } else {
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showTheKeyboardNow()
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.doOnApplyWindowInsets(f: (View, WindowInsets, InitialPadding) -> Unit) {
    // Create a snapshot of the view's padding state
    val initialPadding = recordInitialPaddingForView(this)
    // Set an actual OnApplyWindowInsetsListener which proxies to the given
    // lambda, also passing in the original padding state
    setOnApplyWindowInsetsListener { v, insets ->
        f(v, insets, initialPadding)
        // Always return the insets, so that children can also use them
        insets
    }
    // request some insets
    requestApplyInsetsWhenAttached()
}

data class InitialPadding(val left: Int, val top: Int,
                          val right: Int, val bottom: Int)

private fun recordInitialPaddingForView(view: View) = InitialPadding(
    view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal
        requestApplyInsets()
    } else {
        // We're not attached to the hierarchy, add a listener to request when we are
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}



/**
 * Base URL cho avatar images
 */
private const val BASE_AVATAR_URL = "https://www.utt-school.site"

/**
 * Load avatar với circle crop (mặc định)
 */
fun ImageView.loadAvatar(
    avatarPath: String?,
    @DrawableRes placeholder: Int = R.drawable.avatar_blank,
    @DrawableRes error: Int = R.drawable.avatar_blank
) {
    loadImage(
        imagePath = avatarPath,
        placeholder = placeholder,
        error = error,
        transform = ImageTransform.CIRCLE
    )
}

/**
 * Load avatar với rounded corners
 */
fun ImageView.loadAvatarRounded(
    avatarPath: String?,
    cornerRadius: Int = 16,
    @DrawableRes placeholder: Int = R.drawable.avatar_blank,
    @DrawableRes error: Int = R.drawable.avatar_blank
) {
    loadImage(
        imagePath = avatarPath,
        placeholder = placeholder,
        error = error,
        transform = ImageTransform.ROUNDED,
        cornerRadius = cornerRadius
    )
}

/**
 * Load avatar không transform (hình chữ nhật)
 */
fun ImageView.loadAvatarSquare(
    avatarPath: String?,
    @DrawableRes placeholder: Int = R.drawable.avatar_blank,
    @DrawableRes error: Int = R.drawable.avatar_blank
) {
    loadImage(
        imagePath = avatarPath,
        placeholder = placeholder,
        error = error,
        transform = ImageTransform.NONE
    )
}

/**
 * Hàm chung để load image với comprehensive error handling
 */
private fun ImageView.loadImage(
    imagePath: String?,
    @DrawableRes placeholder: Int,
    @DrawableRes error: Int,
    transform: ImageTransform,
    cornerRadius: Int = 8
) {
    if (!imagePath.isNullOrEmpty()) {
        val imageUrl = buildImageUrl(imagePath)
        
        // ✅ Check if it's SVG file
        if (isSvgFile(imageUrl)) {
            println("⚠️ SVG file detected, using placeholder: $imageUrl")
            setImageResource(placeholder)
            return
        }
        
        val requestOptions = createRequestOptions(transform, cornerRadius, placeholder, error)
        
        println("🖼️ Loading image:")
        println("   Original path: $imagePath")
        println("   Full URL: $imageUrl")

        Glide.with(context)
            .asBitmap() // ✅ Force bitmap decode only
            .load(imageUrl)
            .apply(requestOptions)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    println("❌ Image load failed for: $imageUrl")
                    println("   Error: ${e?.message}")
                    
                    post { setImageResource(error) }
                    return true
                }
                
                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    println("✅ Image loaded successfully: $imageUrl")
                    return false
                }
            })
            .into(this)
    } else {
        setImageResource(error)
    }
}

/**
 * Check if URL is SVG file
 */
private fun isSvgFile(url: String): Boolean {
    return url.lowercase().contains(".svg")
}

/**
 * Enhanced URL validation
 */
private fun isValidImagePath(path: String): Boolean {
    if (path.isBlank()) return false
    
    val lowerPath = path.lowercase()
    
    // ✅ SVG is now considered invalid for bitmap loading
    if (lowerPath.contains(".svg")) {
        return false
    }
    
    // Valid bitmap image extensions
    val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp")
    val hasValidExtension = imageExtensions.any { lowerPath.endsWith(it) }
    
    // Suspicious content
    val suspiciousContent = listOf(".mp4", ".avi", ".mov", ".mkv", "video/")
    val hasSuspiciousContent = suspiciousContent.any { lowerPath.contains(it) }
    
    return hasValidExtension && !hasSuspiciousContent
}

/**
 * Load image với URL (với enhanced error handling)
 */
fun ImageView.loadUrl(
    imageUrl: String?,
    @DrawableRes placeholder: Int = R.drawable.pic_item_product,
    @DrawableRes error: Int = R.drawable.pic_item_product,
    transform: ImageTransform = ImageTransform.NONE,
    cornerRadius: Int = 8
) {
    if (!imageUrl.isNullOrEmpty()) {
        val requestOptions = createRequestOptions(transform, cornerRadius, placeholder, error)
        
        println("🖼️ Loading URL: $imageUrl")

        Glide.with(context)
            .asBitmap() // ✅ Force bitmap decode
            .load(imageUrl)
            .apply(requestOptions)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    println("❌ URL load failed: $imageUrl")
                    println("   Error: ${e?.message}")
                    e?.rootCauses?.forEach { cause ->
                        println("   Root cause: ${cause.message}")
                    }
                    
                    post { setImageResource(error) }
                    return true
                }
                
                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    println("✅ URL loaded successfully: $imageUrl")
                    return false
                }
            })
            .into(this)
    } else {
        setImageResource(error)
    }
}

/**
 * Enhanced URL building with validation
 */
private fun buildImageUrl(imagePath: String): String {
    val fullUrl = if (imagePath.startsWith("http")) {
        imagePath
    } else {
        "$BASE_AVATAR_URL$imagePath"
    }
    
    // ✅ Validate URL format
    if (!isValidImagePath(imagePath)) {
        println("⚠️ Potentially invalid image path: $imagePath")
    }
    
    return fullUrl
}

/**
 * ✅ Enhanced request options với ANR prevention
 */
private fun createRequestOptions(
    transform: ImageTransform,
    cornerRadius: Int,
    @DrawableRes placeholder: Int,
    @DrawableRes error: Int
): RequestOptions {
    val options = RequestOptions()
        .placeholder(placeholder)
        .error(error)
        .timeout(10000) // ✅ Giảm timeout xuống 10s để tránh ANR
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // ✅ Smart caching
        .skipMemoryCache(false) // ✅ Use memory cache
        .dontAnimate() // ✅ Disable animation để tăng performance
        .override(800, 800) // ✅ Giới hạn kích thước để tránh OOM

    return when (transform) {
        ImageTransform.CIRCLE -> options.transform(CircleCrop())
        ImageTransform.ROUNDED -> options.transform(RoundedCorners(cornerRadius))
        ImageTransform.NONE -> options
    }
}

/**
 * Enum cho các loại transform image
 */
enum class ImageTransform {
    NONE,       // Không transform
    CIRCLE,     // Hình tròn
    ROUNDED     // Góc bo tròn
}

/**
 * Extension function để set placeholder khi loading
 */
fun ImageView.setPlaceholder(@DrawableRes drawableRes: Int) {
    setImageResource(drawableRes)
}

/**
 * Extension function để clear image (set về placeholder)
 */
fun ImageView.clearImage(@DrawableRes placeholder: Int = R.drawable.avatar_blank) {
    Glide.with(context).clear(this)
    setImageResource(placeholder)
}

/**
 * Safe load với SVG detection
 */
fun ImageView.safeLoadImage(
    imageUrl: String?,
    @DrawableRes placeholder: Int = R.drawable.pic_item_product,
    @DrawableRes error: Int = R.drawable.pic_item_product,
    transform: ImageTransform = ImageTransform.NONE,
    cornerRadius: Int = 8
) {
    if (imageUrl.isNullOrEmpty()) {
        setImageResource(error)
        return
    }
    
    // ✅ Check for problematic file types
    val lowerUrl = imageUrl.lowercase()
    if (lowerUrl.contains(".svg") || 
        lowerUrl.contains("video") || 
        lowerUrl.contains(".mp4") ||
        lowerUrl.contains(".avi")) {
        
        println("⚠️ Unsupported file type detected: $imageUrl")
        setImageResource(placeholder)
        return
    }
    
    val requestOptions = createRequestOptions(transform, cornerRadius, placeholder, error)
    
    Glide.with(context)
        .asBitmap()
        .load(imageUrl)
        .apply(requestOptions)
        .listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                println("❌ Safe load failed: $imageUrl")
                post { setImageResource(error) }
                return true
            }
            
            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                println("✅ Safe load success: $imageUrl")
                return false
            }
        })
        .into(this)
}