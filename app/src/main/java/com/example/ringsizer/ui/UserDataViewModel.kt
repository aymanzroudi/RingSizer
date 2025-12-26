package com.example.ringsizer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringsizer.data.remote.ApiService
import com.example.ringsizer.data.remote.FavoriteDto
import com.example.ringsizer.data.remote.FavoriteRequest
import com.example.ringsizer.data.remote.SavedSizeDto
import com.example.ringsizer.data.remote.SavedSizeUpsertRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class UserDataViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    var sizes by mutableStateOf<List<SavedSizeDto>>(emptyList())
        private set

    var favorites by mutableStateOf<List<FavoriteDto>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun load() {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                sizes = api.mySizes()
                favorites = api.myFavorites()
            } catch (e: Exception) {
                error = "${e::class.simpleName}: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun upsertSize(type: String, body: SavedSizeUpsertRequest) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                api.upsertMySize(type, body)
                sizes = api.mySizes()
            } catch (e: Exception) {
                error = "${e::class.simpleName}: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun addFavorite(productId: Long) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                api.addFavorite(FavoriteRequest(product_id = productId))
                favorites = api.myFavorites()
            } catch (e: Exception) {
                error = "${e::class.simpleName}: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun removeFavorite(productId: Long) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                api.removeFavorite(productId)
                favorites = api.myFavorites()
            } catch (e: Exception) {
                error = "${e::class.simpleName}: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun isFavorite(productId: Long): Boolean = favorites.any { it.product_id == productId }

    fun ringSize(): SavedSizeDto? = sizes.firstOrNull { it.type.lowercase() == "ring" }

    fun braceletSize(): SavedSizeDto? = sizes.firstOrNull { it.type.lowercase() == "bracelet" }
}
