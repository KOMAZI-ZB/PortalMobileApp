// file: viewmodels/AuthViewModel.kt
package com.example.portalapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.portalapp.data.repository.AuthRepository
import com.example.portalapp.storage.UserPrefs
import com.example.portalapp.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val prefs: UserPrefs
) : ViewModel() {

    // Expose token to UI (fixes CompositionLocal issue)
    val tokenFlow = prefs.tokenFlow()
    suspend fun tokenOnce(): String? = prefs.token()

    data class UiState(
        val userName: String = "",
        val password: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    fun onUserChanged(s: String) = _ui.update { it.copy(userName = s) }
    fun onPassChanged(s: String) = _ui.update { it.copy(password = s) }

    fun login(onSuccess: () -> Unit) = viewModelScope.launch {
        val user = _ui.value.userName.trim()
        val pass = _ui.value.password
        if (user.isEmpty() || pass.isEmpty()) {
            _ui.update { it.copy(error = "Please enter both username and password.") }
            return@launch
        }

        _ui.update { it.copy(loading = true, error = null) }
        when (val res = repo.login(user, pass)) {
            is Result.Success -> {
                prefs.setToken(res.value.token)
                prefs.setUserBasics(res.value.userName, res.value.roles, res.value.joinDate)
                _ui.update { it.copy(loading = false) }
                onSuccess()
            }
            is Result.Error -> _ui.update { it.copy(loading = false, error = res.message) }
        }
    }

    fun logout(onDone: () -> Unit) = viewModelScope.launch {
        prefs.clear()
        onDone()
    }
}
