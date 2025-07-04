package com.passkeydemo.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passkeydemo.android.data.models.Passkey
import com.passkeydemo.android.data.models.UserProfile
import com.passkeydemo.android.data.repository.PasskeyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val passkeys: List<Passkey> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: PasskeyRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        // Observe current user from repository
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                _uiState.update { it.copy(userProfile = user) }
            }
        }
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Load user profile if not already loaded
            if (_uiState.value.userProfile == null) {
                repository.currentUser.value?.let { user ->
                    _uiState.update { it.copy(userProfile = user) }
                }
            }
            
            // Load passkeys
            repository.getUserPasskeys().fold(
                onSuccess = { passkeys ->
                    _uiState.update { 
                        it.copy(
                            passkeys = passkeys,
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load passkeys"
                        )
                    }
                }
            )
        }
    }
    
    fun deletePasskey(id: String) {
        viewModelScope.launch {
            repository.deletePasskey(id).fold(
                onSuccess = {
                    // Remove from local list
                    _uiState.update { state ->
                        state.copy(
                            passkeys = state.passkeys.filter { it.id != id }
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            error = exception.message ?: "Failed to delete passkey"
                        )
                    }
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.logout().fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isLoggedOut = true
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to logout"
                        )
                    }
                }
            )
        }
    }
}