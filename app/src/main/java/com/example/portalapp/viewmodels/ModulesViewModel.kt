package com.example.portalapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portalapp.data.repository.ModulesRepository
import com.example.portalapp.models.Module
import com.example.portalapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModulesViewModel @Inject constructor(
    private val repo: ModulesRepository
) : ViewModel() {

    data class UiState(
        val semester: Int = 1,
        val loading: Boolean = false,
        val error: String? = null,
        val items: List<Module> = emptyList()
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    init {
        // initial load happens once; avoids duplicate calls when navigating back
        refresh()
    }

    fun setSemester(semester: Int) {
        if (semester == _ui.value.semester) return
        _ui.update { it.copy(semester = semester) }
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        val sem = _ui.value.semester
        _ui.update { it.copy(loading = true, error = null) }
        when (val res = repo.bySemester(sem)) {
            is Result.Success -> _ui.update { it.copy(items = res.value, loading = false) }
            is Result.Error   -> _ui.update { it.copy(loading = false, error = res.message) }
        }
    }
}
