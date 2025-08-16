package com.example.portalapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portalapp.data.repository.RepositoryRepository
import com.example.portalapp.models.Document
import com.example.portalapp.models.RepositoryLink
import com.example.portalapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepositoryViewModel @Inject constructor(
    private val repo: RepositoryRepository
) : ViewModel() {

    data class UiState(
        val external: List<RepositoryLink> = emptyList(),
        val internal: List<Document> = emptyList(),
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState(loading = true))
    val ui: StateFlow<UiState> = _ui

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _ui.update { it.copy(loading = true, error = null) }

        val extRes = repo.external()
        val intRes = repo.internal()

        val external = if (extRes is Result.Success) extRes.value else emptyList()
        val internal = if (intRes is Result.Success) intRes.value else emptyList()

        val errorMsg = when {
            extRes is Result.Error && intRes is Result.Error ->
                extRes.message // show one; both failed
            extRes is Result.Error -> extRes.message
            intRes is Result.Error -> intRes.message
            else -> null
        }

        _ui.update { it.copy(external = external, internal = internal, loading = false, error = errorMsg) }
    }
}
