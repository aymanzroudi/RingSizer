package com.example.ringsizer.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.MultipartBody

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "buyer",
    val shop_name: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val address: String? = null
)

data class AuthRequest(val email: String, val password: String)
data class UserDto(val id: Long, val name: String, val email: String, val role: String)
data class AuthResponse(val user: UserDto, val token: String)

data class SellerProfileDto(
    val shop_name: String?,
    val phone: String?,
    val city: String?,
    val address: String?
)

data class SellerDto(
    val id: Long,
    val name: String,
    val email: String?,
    val role: String?,
    val seller_profile: SellerProfileDto? = null
)

data class ImageDto(
    val path: String?
)

data class ProductDto(
    val id: Long,
    val seller_id: Long?,
    val seller: SellerDto? = null,
    val title: String,
    val description: String?,
    val category: String?,
    val size_min_mm: Double? = null,
    val size_max_mm: Double? = null,
    val karat: Int,
    val weight_g: Double?,
    val price: Double,
    val stock: Int,
    val status: String,
    val cover_image_path: String?,
    val images: List<ImageDto>? = emptyList()
)

data class ProductListResponse(
    val data: List<ProductDto>
)

data class MessageResponse(
    val message: String?
)

data class CreateProductRequest(
    val title: String,
    val description: String?,
    val category: String?,
    val size_min_mm: Double? = null,
    val size_max_mm: Double? = null,
    val karat: Int,
    val weight_g: Double?,
    val price: Double,
    val stock: Int,
    val status: String = "published"
)

data class AddImageRequest(
    val path: String,
    val position: Int? = null
)

data class ProductImageDto(
    val id: Long,
    val product_id: Long,
    val path: String,
    val position: Int?
)

data class GoldPriceDto(
    val id: Long,
    val source: String,
    val base_currency: String,
    val price_per_ounce: Double,
    val price_per_gram: Double,
    val price_per_gram_mad: Double?,
    val karat: Int,
    val collected_at: String
)

data class SavedSizeDto(
    val id: Long,
    val user_id: Long,
    val type: String,
    val diameter_mm: Double?,
    val circumference_mm: Double?,
    val standard: String?,
    val label: String?
)

data class SavedSizeUpsertRequest(
    val diameter_mm: Double? = null,
    val circumference_mm: Double? = null,
    val standard: String? = null,
    val label: String? = null
)

data class FavoriteRequest(val product_id: Long)

data class FavoriteDto(
    val id: Long,
    val user_id: Long,
    val product_id: Long,
    val product: ProductDto? = null
)

data class CartAddRequest(val product_id: Long, val quantity: Int? = 1)
data class CartUpdateRequest(val quantity: Int)

data class CartItemDto(
    val id: Long,
    val user_id: Long,
    val product_id: Long,
    val quantity: Int,
    val product: ProductDto? = null
)

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: AuthRequest): AuthResponse

    @GET("api/products")
    suspend fun products(): ProductListResponse

    @POST("api/products")
    suspend fun createProduct(@Body body: CreateProductRequest): ProductDto

    @PUT("api/products/{productId}")
    suspend fun updateProductPut(@Path("productId") productId: Long, @Body body: CreateProductRequest): ProductDto

    @PATCH("api/products/{productId}")
    suspend fun updateProductPatch(@Path("productId") productId: Long, @Body body: CreateProductRequest): ProductDto

    @DELETE("api/products/{productId}")
    suspend fun deleteProduct(@Path("productId") productId: Long): MessageResponse

    @POST("api/products/{productId}/images")
    suspend fun addProductImage(@Path("productId") productId: Long, @Body body: AddImageRequest): ProductImageDto

    @Multipart
    @POST("api/products/{productId}/upload-image")
    suspend fun uploadCoverImage(@Path("productId") productId: Long, @Part image: MultipartBody.Part): ProductDto

    @GET("api/gold/latest")
    suspend fun goldLatest(): GoldPriceDto

    @GET("api/gold/history")
    suspend fun goldHistory(
        @Query("days") days: Int = 30,
        @Query("karat") karat: Int = 24
    ): List<GoldPriceDto>

    @GET("api/me/sizes")
    suspend fun mySizes(): List<SavedSizeDto>

    @PUT("api/me/sizes/{type}")
    suspend fun upsertMySize(
        @Path("type") type: String,
        @Body body: SavedSizeUpsertRequest
    ): SavedSizeDto

    @DELETE("api/me/sizes/{id}")
    suspend fun deleteMySize(@Path("id") id: Long): MessageResponse

    @GET("api/me/favorites")
    suspend fun myFavorites(): List<FavoriteDto>

    @POST("api/me/favorites")
    suspend fun addFavorite(@Body body: FavoriteRequest): FavoriteDto

    @DELETE("api/me/favorites/{productId}")
    suspend fun removeFavorite(@Path("productId") productId: Long): MessageResponse

    @GET("api/cart")
    suspend fun cart(): List<CartItemDto>

    @POST("api/cart")
    suspend fun addToCart(@Body body: CartAddRequest): CartItemDto

    @PUT("api/cart/{productId}")
    suspend fun updateCartItem(
        @Path("productId") productId: Long,
        @Body body: CartUpdateRequest
    ): CartItemDto

    @DELETE("api/cart/{productId}")
    suspend fun removeFromCart(@Path("productId") productId: Long): MessageResponse

    @DELETE("api/cart")
    suspend fun clearCart(): MessageResponse
}