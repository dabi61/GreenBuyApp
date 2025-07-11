package com.example.greenbuyapp.domain.product

import com.example.greenbuyapp.data.product.ProductService
import com.example.greenbuyapp.data.product.model.Product
import com.example.greenbuyapp.data.product.model.ProductAttributeList
import com.example.greenbuyapp.data.product.model.ProductListResponse
import com.example.greenbuyapp.data.product.model.TrendingProductResponse
import com.example.greenbuyapp.data.product.model.shopProducts
import com.example.greenbuyapp.data.product.model.InventoryStatsResponse
import com.example.greenbuyapp.data.product.model.ProductsByStatusResponse
import com.example.greenbuyapp.data.product.model.CreateProductResponse
import com.example.greenbuyapp.data.product.model.CreateAttributeResponse
import com.example.greenbuyapp.util.MultipartUtils
import android.content.Context
import android.net.Uri
import com.example.greenbuyapp.data.MessageResponse
import com.example.greenbuyapp.util.Result
import com.example.greenbuyapp.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import com.example.greenbuyapp.data.product.model.ApproveProductRequest
import kotlinx.coroutines.withTimeout
import com.example.greenbuyapp.data.product.model.ProductAttribute

class ProductRepository(
    private val productService: ProductService,
    private val shopService: com.example.greenbuyapp.data.shop.ShopService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Lấy danh sách sản phẩm với StateFlow architecture
     * @param page Số trang (mặc định là 1)
     * @param limit Số lượng sản phẩm trên một trang (mặc định là 10)
     * @param search Từ khóa tìm kiếm
     * @param categoryId ID danh mục
     * @param subCategoryId ID danh mục con
     * @param shopId ID cửa hàng
     * @param minPrice Giá tối thiểu
     * @param maxPrice Giá tối đa
     * @param sortBy Sắp xếp theo (mặc định: created_at)
     * @param sortOrder Thứ tự sắp xếp (mặc định: desc)
     * @param approvedOnly Chỉ lấy sản phẩm được duyệt (mặc định: true)
     * @return Result<ProductListResponse> chứa danh sách sản phẩm
     */
    suspend fun getProducts(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        categoryId: Int? = null,
        subCategoryId: Int? = null,
        shopId: Int? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: String = "created_at",
        sortOrder: String = "desc",
        approvedOnly: Boolean = true
    ): Result<ProductListResponse> {
        return safeApiCall(dispatcher) {
            productService.getProducts(
                page = page,
                limit = limit,
                search = search,
                categoryId = categoryId,
                subCategoryId = subCategoryId,
                shopId = shopId,
                minPrice = minPrice,
                maxPrice = maxPrice,
                sortBy = sortBy,
                sortOrder = sortOrder,
                approvedOnly = approvedOnly
            )
        }
    }

    /**
     * Lấy danh sách sản phẩm trending
     * @param page Số trang (mặc định là 1)
     * @param limit Số lượng sản phẩm trên một trang (mặc định là 10)
     * @return Result<TrendingProductResponse> chứa danh sách sản phẩm trending
     */
    suspend fun getTrending(
        page: Int? = null,
        limit: Int? = null
    ): Result<TrendingProductResponse> {
        return safeApiCall(dispatcher) {
            productService.getTrending(
                page = page,
                limit = limit
            )
        }
    }

    /**
     * Lấy attributes của sản phẩm theo ID
     * @param productId ID của sản phẩm
     * @return Result<ProductAttributeList> chứa danh sách attributes
     */
    suspend fun getProductAttributes(productId: Int): Result<ProductAttributeList> {
        return safeApiCall(dispatcher) {
            productService.getProductAttributes(productId)
        }
    }

    /**
     * ✅ Lấy attribute theo ID
     * @param attributeId ID của attribute
     * @return Result<ProductAttribute> chứa thông tin attribute
     */
    suspend fun getAttribute(attributeId: Int): Result<ProductAttribute> {
        return safeApiCall(dispatcher) {
            productService.getAttribute(attributeId)
        }
    }

    /**
     * Lấy sản phẩm theo ID
     * @param productId ID của sản phẩm
     * @return Result<ProductAttributeList> chứa danh sách attributes
     */
    suspend fun getProduct(productId: Int): Result<Product> {
        return safeApiCall(dispatcher) {
            productService.getProduct(productId)
        }
    }

    /**
     * Lấy danh sách sản phẩm theo shop id
     * @param productId ID của sản phẩm
     * @return Result<List<Product>> chứa danh sách product
     */
    suspend fun getProductsByShopId(
        shopId: Int,
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        sortBy: String = "created_at",
        sortOrder: String = "desc",
        approvedOnly: Boolean = true
    ): Result<ProductListResponse> {
        return safeApiCall(dispatcher) {
            productService.getProductsByShopId(shopId)
        }
    }

    /**
     * Lấy thống kê inventory của shop hiện tại
     * @return Result<InventoryStatsResponse> chứa thống kê inventory
     */
    suspend fun getInventoryStats(): Result<InventoryStatsResponse> {
        return safeApiCall(dispatcher) {
            productService.getInventoryStats()
        }
    }

    /**
     * Lấy sản phẩm theo trạng thái
     * @param status Trạng thái sản phẩm (in_stock, out_of_stock, pending_approval)
     * @param page Số trang (mặc định là 1)
     * @param limit Số lượng sản phẩm trên một trang (mặc định là 10)
     * @param search Từ khóa tìm kiếm
     * @return Result<ProductsByStatusResponse> chứa danh sách sản phẩm theo trạng thái
     */
    suspend fun getProductsByStatus(
        status: String,
        page: Int = 1,
        limit: Int = 10,
        search: String? = null
    ): Result<ProductsByStatusResponse> {
        return safeApiCall(dispatcher) {
            productService.getProductsByStatus(status, page, limit, search)
        }
    }

    /**
     * Tạo sản phẩm mới với progress tracking
     */
    suspend fun createProduct(
        context: Context,
        name: String,
        description: String,
        price: Double,
        subCategoryId: Int,
        coverUri: Uri
    ): Result<CreateProductResponse> {
        return safeApiCall(dispatcher) {
            println("🚀 Starting product creation...")
            val startTime = System.currentTimeMillis()
            
            try {
                // ✅ Thêm timeout 120 giây cho upload
                withTimeout(120000L) {
                    val namePart = MultipartUtils.createTextPart(name)
                    val descriptionPart = MultipartUtils.createTextPart(description)
                    val pricePart = MultipartUtils.createTextPart(price.toString())
                    val subCategoryPart = MultipartUtils.createTextPart(subCategoryId.toString())
                    
                    println("📸 Processing cover image...")
                    val coverPart = MultipartUtils.createImagePart(context, "cover", coverUri)
                        ?: throw IllegalArgumentException("Cannot create image part from Uri")

                    println("📤 Uploading to server...")
                    val response = productService.createProduct(
                        name = namePart,
                        description = descriptionPart,
                        price = pricePart,
                        subCategoryId = subCategoryPart,
                        cover = coverPart
                    )
                    
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    println("✅ Product created successfully in ${duration}ms")
                    println("   Product ID: ${response.product_id}")
                    println("   Product name: ${response.name}")
                    
                    response
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                println("⏰ Upload timeout after 120 seconds")
                throw Exception("Upload timeout: Quá thời gian chờ upload ảnh")
            } catch (e: Exception) {
                println("❌ Upload error: ${e.message}")
                throw e
            }
        }
    }

    /**
     * Tạo sản phẩm mới
     * @param context Context để xử lý Uri
     * @param name Tên sản phẩm
     * @param description Mô tả sản phẩm
     * @param price Giá sản phẩm
     * @param subCategoryId ID danh mục con
     * @param coverUri Uri của ảnh cover
     * @return Result<CreateProductResponse> chứa thông tin sản phẩm đã tạo
     */
    suspend fun editProduct(
        context: Context,
        productId: Int,
        name: String,
        description: String,
        price: Double,
        subCategoryId: Int,
        coverUri: Uri
    ): Result<CreateProductResponse> {
        return safeApiCall(dispatcher) {
            val namePart = MultipartUtils.createTextPart(name)
            val descriptionPart = MultipartUtils.createTextPart(description)
            val pricePart = MultipartUtils.createTextPart(price.toString())
            val subCategoryPart = MultipartUtils.createTextPart(subCategoryId.toString())
            val coverPart = MultipartUtils.createImagePart(context, "cover", coverUri)
                ?: throw IllegalArgumentException("Cannot create image part from Uri")

            productService.editProduct(
                productId = productId,
                name = namePart,
                description = descriptionPart,
                price = pricePart,
                subCategoryId = subCategoryPart,
                cover = coverPart
            )
        }
    }

    /**
     * ✅ Chỉnh sửa sản phẩm chỉ text fields (không có ảnh mới)
     * @param productId ID sản phẩm
     * @param name Tên sản phẩm
     * @param description Mô tả sản phẩm
     * @param price Giá sản phẩm
     * @param subCategoryId ID danh mục con
     * @return Result<CreateProductResponse> chứa thông tin sản phẩm đã cập nhật
     */
    suspend fun editProductText(
        productId: Int,
        name: String,
        description: String,
        price: Double,
        subCategoryId: Int
    ): Result<CreateProductResponse> {
        return safeApiCall(dispatcher) {
            val namePart = MultipartUtils.createTextPart(name)
            val descriptionPart = MultipartUtils.createTextPart(description)
            val pricePart = MultipartUtils.createTextPart(price.toString())
            val subCategoryPart = MultipartUtils.createTextPart(subCategoryId.toString())
            
            productService.editProductText(
                productId = productId,
                name = namePart,
                description = descriptionPart,
                price = pricePart,
                subCategoryId = subCategoryPart,
                cover = null
            )
        }
    }

    /**
     * Tạo attribute cho sản phẩm
     * @param context Context để xử lý Uri
     * @param productId ID sản phẩm
     * @param color Màu sắc
     * @param size Kích thước
     * @param price Giá
     * @param quantity Số lượng
     * @param imageUri Uri của ảnh
     * @return Result<CreateAttributeResponse> chứa thông tin attribute đã tạo
     */
    suspend fun createAttribute(
        context: Context,
        productId: Int,
        color: String,
        size: String,
        price: Double,
        quantity: Int,
        imageUri: Uri
    ): Result<CreateAttributeResponse> {
        return safeApiCall(dispatcher) {
            val productIdPart = MultipartUtils.createTextPart(productId.toString())
            val colorPart = MultipartUtils.createTextPart(color)
            val sizePart = MultipartUtils.createTextPart(size)
            val pricePart = MultipartUtils.createTextPart(price.toString())
            val quantityPart = MultipartUtils.createTextPart(quantity.toString())
            val imagePart = MultipartUtils.createImagePart(context, "image", imageUri)
                ?: throw IllegalArgumentException("Cannot create image part from Uri")

            productService.createAttribute(
                productId = productIdPart,
                color = colorPart,
                size = sizePart,
                price = pricePart,
                quantity = quantityPart,
                image = imagePart
            )
        }
    }

    /**
     * ✅ Chỉnh sửa attribute của sản phẩm
     * @param context Context để xử lý Uri
     * @param attributeId AttributeID
     * @param productId ID sản phẩm
     * @param color Màu sắc
     * @param size Kích thước
     * @param price Giá
     * @param quantity Số lượng
     * @param imageUri Uri của ảnh (required)
     * @return Result<CreateAttributeResponse> chứa thông tin attribute đã cập nhật
     */
    suspend fun editAttribute(
        context: Context,
        attributeId: Int,
        productId: Int,
        color: String,
        size: String,
        price: Double,
        quantity: Int,
        imageUri: Uri
    ): Result<CreateAttributeResponse> {
        return safeApiCall(dispatcher) {
            val productIdPart = MultipartUtils.createTextPart(productId.toString())
            val colorPart = MultipartUtils.createTextPart(color)
            val sizePart = MultipartUtils.createTextPart(size)
            val pricePart = MultipartUtils.createTextPart(price.toString())
            val quantityPart = MultipartUtils.createTextPart(quantity.toString())
            val imagePart = MultipartUtils.createImagePart(context, "image", imageUri)
                ?: throw IllegalArgumentException("Cannot create image part from Uri")

            productService.editAttribute(
                attributeId = attributeId,
                productId = productIdPart,
                color = colorPart,
                size = sizePart,
                price = pricePart,
                quantity = quantityPart,
                image = imagePart
            )
        }
    }

    /**
     * ✅ Chỉnh sửa attribute chỉ text fields (không có ảnh mới)
     * @param context Context để xử lý Uri
     * @param attributeId AttributeID
     * @param productId ID sản phẩm
     * @param color Màu sắc
     * @param size Kích thước
     * @param price Giá
     * @param quantity Số lượng
     * @param oldImageUrl URL của ảnh cũ
     * @return Result<CreateAttributeResponse> chứa thông tin attribute đã cập nhật
     */
    suspend fun editAttributeText(
        context: Context,
        attributeId: Int,
        productId: Int,
        color: String,
        size: String,
        price: Double,
        quantity: Int,
        oldImageUrl: String
    ): Result<CreateAttributeResponse> {
        return safeApiCall(dispatcher) {
            val productIdPart = MultipartUtils.createTextPart(productId.toString())
            val colorPart = MultipartUtils.createTextPart(color)
            val sizePart = MultipartUtils.createTextPart(size)
            val pricePart = MultipartUtils.createTextPart(price.toString())
            val quantityPart = MultipartUtils.createTextPart(quantity.toString())
            
            // Tạo file ảnh từ URL cũ
            var oldImagePart = MultipartUtils.createImagePartFromUrl(context, "image", oldImageUrl)
            
            // Nếu không download được ảnh cũ, tạo file dummy
            if (oldImagePart == null) {
                println("⚠️ Failed to download old image, creating dummy image instead")
                oldImagePart = MultipartUtils.createDummyImagePart(context, "image")
            }

            productService.editAttributeText(
                attributeId = attributeId,
                productId = productIdPart,
                color = colorPart,
                size = sizePart,
                price = pricePart,
                quantity = quantityPart,
                image = oldImagePart
            )
        }
    }

    suspend fun deleteAttribute(attributeId: Int): Result<MessageResponse> {
        return safeApiCall(dispatcher) {
            productService.deleteAttribute(attributeId)
        }
    }

    /**
     * ✅ Duyệt sản phẩm
     * @param productId ID sản phẩm cần duyệt
     * @param approvalNote Ghi chú duyệt (optional)
     * @return Result<Product> kết quả duyệt
     */
    suspend fun approveProduct(productId: Int, approvalNote: String = "Đã duyệt"): Result<Product> {
        return safeApiCall(dispatcher) {
            val requestBody = ApproveProductRequest(
                approved = true,
                approval_note = approvalNote
            )
            productService.approveProduct(productId, requestBody)
        }
    }

    /**
     * ✅ Từ chối sản phẩm
     * @param productId ID sản phẩm cần từ chối
     * @param reason Lý do từ chối
     * @return Result<Product> kết quả từ chối
     */
    suspend fun rejectProduct(productId: Int, reason: String = "Đã từ chối"): Result<Product> {
        return safeApiCall(dispatcher) {
            val requestBody = ApproveProductRequest(
                approved = false,
                approval_note = reason
            )
            productService.rejectProduct(productId, requestBody)
        }
    }

    /**
     * ✅ Lấy danh sách sản phẩm chờ duyệt
     * @return Result<List<PendingApprovalProduct>> danh sách sản phẩm chờ duyệt
     */
    suspend fun getPendingApprovalProducts(): Result<List<com.example.greenbuyapp.data.product.model.PendingApprovalProduct>> {
        return safeApiCall(dispatcher) {
            productService.getPendingApprovalProducts()
        }
    }

    /**
     * ✅ Lấy thông tin shop theo ID
     * @param shopId ID của shop
     * @return Result<Shop> thông tin shop
     */
    suspend fun getShopById(shopId: Int): Result<com.example.greenbuyapp.data.shop.model.Shop> {
        return safeApiCall(dispatcher) {
            shopService.getShopById(shopId)
        }
    }

} 