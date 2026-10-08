package com.example.workapp.automation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KoogAutomationUiState(
    val isRunning: Boolean = false,
    val response: String? = null,
    val error: String? = null
)

class KoogAutomationViewModel(private val openAiApiKey: String) : ViewModel() {
    private val _uiState = MutableStateFlow(KoogAutomationUiState())
    val uiState = _uiState.asStateFlow()

    private val agent by lazy { KoogScreenAutomationAgent(openAiApiKey) }

    fun canStartInstruction(): Boolean =
        openAiApiKey.isNotBlank() && AppAutomationAccessibilityService.isEnabled()

    fun runInstruction(instruction: String) {
        if (openAiApiKey.isBlank()) {
            _uiState.update { it.copy(error = "Add OPENAI_API_KEY to local.properties or the environment, then rebuild.") }
            return
        }
        if (!AppAutomationAccessibilityService.isEnabled()) {
            _uiState.update { it.copy(error = "Enable WorkApp screen automation in Android Accessibility settings first.") }
            return
        }
        if (instruction.isBlank()) {
            _uiState.update { it.copy(error = "Enter an automation instruction first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true, response = null, error = null) }
            runCatching { agent.run(instruction.trim()) }
                .onSuccess { answer -> _uiState.update { it.copy(isRunning = false, response = answer) } }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isRunning = false, error = error.message ?: "Koog could not complete the automation.")
                    }
                }
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(response = null, error = null) }
    }
}
