package com.example.ringsizer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringsizer.data.remote.ApiService
import com.example.ringsizer.data.remote.CartAddRequest
import com.example.ringsizer.data.remote.CartItemDto
import com.example.ringsizer.data.remote.CartUpdateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CartViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    var items by mutableStateOf<List<CartItemDto>>(emptyList())
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
                items = api.cart()
            } catch (e: Exception) {
                error = "${e::class.simpleName}: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun add(productId: Long, quantity: Int = 1) {
        viewModelScope.launch {
            val before = items
            loading = true
            error = null
            try {
                // optimistic
                val existing = items.firstOrNull { it.product_id == productId }
                items = if (existing != null) {
                    items.map {
                        if (it.product_id == productId) it.copy(quantity = it.quantity + quantity) else it
                    }
                } else {
                    items
                }

                api.addToCart(CartAddRequest(product_id = productId, quantity = quantity))

                // ensure we have product hydrated for new items
                items = api.cart()
            } catch (e: Exception) {
                items = before
                error = "${e::class.simpleName}: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun update(productId: Long, quantity: Int) {
        viewModelScope.launch {
            val before = items
            error = null
            try {
                // optimistic
                items = items.map {
                    if (it.product_id == productId) it.copy(quantity = quantity) else it
                }
                api.updateCartItem(productId, CartUpdateRequest(quantity = quantity))
            } catch (e: Exception) {
                items = before
                error = "${e::class.simpleName}: ${e.message}"
            }
        }
    }

    fun remove(productId: Long) {
        viewModelScope.launch {
            val before = items
            error = null
            try {
                // optimistic
                items = items.filterNot { it.product_id == productId }
                api.removeFromCart(productId)
            } catch (e: Exception) {
                items = before
                error = "${e::class.simpleName}: ${e.message}"
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                api.clearCart()
                items = emptyList()
            } catch (e: Exception) {
                error = "${e::class.simpleName}: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun totalMad(): Double = items.sumOf { (it.product?.price ?: 0.0) * it.quantity }
}
