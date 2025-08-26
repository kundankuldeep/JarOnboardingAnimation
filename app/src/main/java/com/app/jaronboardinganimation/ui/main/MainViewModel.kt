package com.app.jaronboardinganimation.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    fun updateMessage(message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                message = message,
                isLoading = false
            )
        }
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Simulate network call
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(
                message = "Data loaded successfully!",
                isLoading = false
            )
        }
    }
}

data class MainUiState(
    val message: String = "Welcome to MVVM with Hilt!",
    val isLoading: Boolean = false
)
