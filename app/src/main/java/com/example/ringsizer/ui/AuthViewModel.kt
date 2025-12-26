package com.example.ringsizer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringsizer.data.local.TokenManager
import com.example.ringsizer.data.remote.ApiService
import com.example.ringsizer.data.remote.AuthRequest
import com.example.ringsizer.data.remote.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    var token: String? by mutableStateOf(null)
        private set
    var userId: Long? by mutableStateOf(null)
        private set
    var userRole: String? by mutableStateOf(null)
        private set
    var userName: String? by mutableStateOf(null)
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            tokenManager.tokenFlow.collectLatest { token = it }
        }
        viewModelScope.launch {
            tokenManager.userIdFlow.collectLatest { userId = it }
        }
        viewModelScope.launch {
            tokenManager.userRoleFlow.collectLatest { userRole = it }
        }
        viewModelScope.launch {
            tokenManager.userNameFlow.collectLatest { userName = it }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loading = true; error = null
            try {
                val res = api.login(AuthRequest(email, password))
                tokenManager.setToken(res.token)
                tokenManager.setUser(res.user.id, res.user.role, res.user.name)
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        role: String = "buyer",
        shopName: String? = null,
        phone: String? = null,
        city: String? = null,
        address: String? = null
    ) {
        viewModelScope.launch {
            loading = true; error = null
            try {
                val res = api.register(
                    RegisterRequest(
                        name = name,
                        email = email,
                        password = password,
                        role = role,
                        shop_name = shopName,
                        phone = phone,
                        city = city,
                        address = address
                    )
                )
                tokenManager.setToken(res.token)
                tokenManager.setUser(res.user.id, res.user.role, res.user.name)
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clear()
        }
    }
}
