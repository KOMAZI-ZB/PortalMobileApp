package com.example.portalapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portalapp.data.repository.FaqRepository
import com.example.portalapp.models.FaqEntry
import com.example.portalapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaqViewModel @Inject constructor(
    private val repo: FaqRepository
) : ViewModel() {

    data class UiState(
        val items: List<FaqEntry> = emptyList(),
        val loading: Boolean = false,
        val error: String? = null,
        val search: String = ""
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    fun onSearchChange(s: String) = _ui.update { it.copy(search = s) }

    fun refresh() = viewModelScope.launch {
        _ui.update { it.copy(loading = true, error = null) }

        // We still let the server help narrow results using the raw search string,
        // but we ALSO apply client-side keyword filtering to support multiple keywords.
        val rawSearch = _ui.value.search.trim().ifBlank { null }

        when (val res = repo.fetch(page = 1, size = 50, search = rawSearch)) {
            is Result.Success -> {
                val filtered = filterByKeywords(res.value, _ui.value.search)
                _ui.update { it.copy(items = filtered, loading = false) }
            }
            is Result.Error -> _ui.update { it.copy(loading = false, error = res.message) }
        }
    }

    fun applySearch() = refresh()

    // --- Keyword filter (supports space/comma separated terms; matches ALL terms) ---
    private fun filterByKeywords(list: List<FaqEntry>, query: String): List<FaqEntry> {
        val keywords = query
            .lowercase()
            .split(',', ' ')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (keywords.isEmpty()) return list

        return list.filter { entry ->
            val haystack = "${entry.question} ${entry.answer}".lowercase()
            keywords.all { kw -> haystack.contains(kw) }
        }
    }
}
