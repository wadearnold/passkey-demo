package com.passkeydemo.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passkeydemo.android.data.repository.PasskeyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val username: String = "",
    val displayName: String = "",
    val usernameError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: PasskeyRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    private val usernameRegex = Regex("^[a-zA-Z0-9._-]{3,30}$")
    
    fun updateUsername(username: String) {
        _uiState.update { state ->
            state.copy(
                username = username,
                usernameError = validateUsername(username),
                error = null
            )
        }
    }
    
    fun updateDisplayName(displayName: String) {
        _uiState.update { state ->
            state.copy(
                displayName = displayName,
                error = null
            )
        }
    }
    
    fun register() {
        val currentState = _uiState.value
        if (currentState.username.isBlank() || currentState.displayName.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all fields") }
            return
        }
        
        if (currentState.usernameError != null) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            repository.register(
                username = currentState.username.trim(),
                displayName = currentState.displayName.trim()
            ).fold(
                onSuccess = { userProfile ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Registration failed"
                        )
                    }
                }
            )
        }
    }
    
    private fun validateUsername(username: String): String? {
        return when {
            username.isEmpty() -> null
            username.length < 3 -> "Username must be at least 3 characters"
            username.length > 30 -> "Username must be less than 30 characters"
            !usernameRegex.matches(username) -> "Username can only contain letters, numbers, dots, underscores, and hyphens"
            else -> null
        }
    }
}