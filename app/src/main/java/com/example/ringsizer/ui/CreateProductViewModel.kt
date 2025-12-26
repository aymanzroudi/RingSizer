package com.example.ringsizer.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringsizer.data.remote.ApiService
import com.example.ringsizer.data.remote.CreateProductRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@HiltViewModel
class CreateProductViewModel @Inject constructor(
    private val api: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var createdId by mutableStateOf<Long?>(null)
        private set

    fun create(
        title: String,
        description: String?,
        category: String?,
        sizeMinMm: Double?,
        sizeMaxMm: Double?,
        karat: Int,
        weightG: Double?,
        price: Double,
        stock: Int,
        imagePath: String? = null
    ) {
        viewModelScope.launch {
            loading = true
            error = null
            createdId = null
            try {
                val res = api.createProduct(
                    CreateProductRequest(
                        title = title,
                        description = description,
                        category = category,
                        size_min_mm = sizeMinMm,
                        size_max_mm = sizeMaxMm,
                        karat = karat,
                        weight_g = weightG,
                        price = price,
                        stock = stock
                    )
                )
                createdId = res.id

                if (!imagePath.isNullOrBlank()) {
                    uploadCover(res.id, imagePath)
                }
            } catch (e: Exception) {
                error = "${e::class.simpleName}: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    private suspend fun uploadCover(productId: Long, uriString: String) {
        val uri = Uri.parse(uriString)
        val mime = context.contentResolver.getType(uri) ?: "image/*"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Cannot read image")

        val body = bytes.toRequestBody(mime.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("image", "cover.jpg", body)

        api.uploadCoverImage(productId, part)
    }
}
