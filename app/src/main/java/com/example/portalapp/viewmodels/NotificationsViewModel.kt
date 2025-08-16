package com.example.portalapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portalapp.data.repository.NotificationsRepository
import com.example.portalapp.models.Notification
import com.example.portalapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repo: NotificationsRepository
) : ViewModel() {

    data class UiState(
        val items: List<Notification> = emptyList(),
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    fun refresh() = viewModelScope.launch {
        _ui.update { it.copy(loading = true, error = null) }
        when (val res = repo.fetch()) {
            is Result.Success -> _ui.update { it.copy(items = res.value, loading = false) }
            is Result.Error   -> _ui.update { it.copy(loading = false, error = res.message) }
        }
    }

    fun markRead(id: Int) = viewModelScope.launch {
        when (repo.markRead(id)) {
            is Result.Success -> {
                _ui.update { state ->
                    state.copy(
                        items = state.items.map { if (it.id == id) it.copy(isRead = true) else it }
                    )
                }
            }
            is Result.Error -> { /* silently ignore for now; keep UI responsive */ }
        }
    }
}
