package com.example.ringsizer.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringsizer.data.remote.ApiService
import com.example.ringsizer.data.remote.ProductDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {
    var items by mutableStateOf<List<ProductDto>>(emptyList())
        private set
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    init { load() }

    fun load() {
        viewModelScope.launch {
            loading = true; error = null
            try { items = api.products().data }
            catch (e: Exception) { error = "${e::class.simpleName}: ${e.message}" }
            finally { loading = false }
        }
    }

    fun removeLocal(productId: Long) {
        items = items.filterNot { it.id == productId }
    }

    fun upsertLocal(product: ProductDto) {
        val idx = items.indexOfFirst { it.id == product.id }
        items = if (idx >= 0) {
            items.toMutableList().also { it[idx] = product }
        } else {
            listOf(product) + items
        }
    }
}