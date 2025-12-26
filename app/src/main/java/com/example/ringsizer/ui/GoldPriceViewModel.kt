package com.example.ringsizer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringsizer.data.remote.ApiService
import com.example.ringsizer.data.remote.GoldPriceDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class GoldPriceViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {
    var price by mutableStateOf<GoldPriceDto?>(null)
        private set
    var history by mutableStateOf<List<GoldPriceDto>>(emptyList())
        private set
    var selectedDays by mutableStateOf(30)
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init { load(days = 30) }

    fun setDays(days: Int) {
        val clamped = days.coerceIn(1, 180)
        if (clamped == selectedDays) return
        selectedDays = clamped
        load(days = clamped)
    }

    fun load(days: Int = selectedDays, karat: Int = 24) {
        viewModelScope.launch {
            loading = true; error = null
            try {
                price = api.goldLatest()
                history = api.goldHistory(days = days, karat = karat)
            } catch (e: Exception) {
                error = "${e::class.simpleName}: ${e.message}"
            }
            finally { loading = false }
        }
    }

    fun latestValueMad(): Double? {
        val p = price ?: return null
        return p.price_per_gram_mad ?: p.price_per_gram
    }

    fun historyValuesMad(): List<Double> {
        return history.map { it.price_per_gram_mad ?: it.price_per_gram }
    }

    fun stats(): Triple<Double?, Double?, Double?> {
        val values = historyValuesMad()
        if (values.isEmpty()) return Triple(null, null, null)
        val min = values.minOrNull()
        val max = values.maxOrNull()
        val change = if (values.size >= 2) values.last() - values.first() else 0.0
        return Triple(min, max, change)
    }
}
