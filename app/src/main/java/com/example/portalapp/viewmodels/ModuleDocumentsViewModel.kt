package com.example.portalapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portalapp.data.repository.DocumentsRepository
import com.example.portalapp.models.Document
import com.example.portalapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModuleDocumentsViewModel @Inject constructor(
    private val repo: DocumentsRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val documents: List<Document> = emptyList()
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    fun load(moduleId: Int) = viewModelScope.launch {
        _ui.update { it.copy(loading = true, error = null) }
        when (val res = repo.byModule(moduleId)) {
            is Result.Success -> _ui.update { it.copy(loading = false, documents = res.value) }
            is Result.Error   -> _ui.update { it.copy(loading = false, error = res.message) }
        }
    }
}
