package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesRepository
import com.example.data.UserProgressState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = PreferencesRepository(application.applicationContext)

    val progressState: StateFlow<UserProgressState> = repo.progressFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProgressState()
    )

    private val _selectedUnitId = MutableStateFlow(1)
    val selectedUnitId: StateFlow<Int> = _selectedUnitId.asStateFlow()

    private val _selectedQuizUnitId = MutableStateFlow(1)
    val selectedQuizUnitId: StateFlow<Int> = _selectedQuizUnitId.asStateFlow()

    fun selectUnit(unitId: Int) {
        _selectedUnitId.value = unitId.coerceIn(1, 5)
    }

    fun selectQuizUnit(unitId: Int) {
        _selectedQuizUnitId.value = unitId.coerceIn(1, 5)
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            repo.setDarkTheme(!progressState.value.isDarkTheme)
        }
    }

    fun recordQuizResult(unitId: Int, score: Int) {
        viewModelScope.launch {
            repo.saveQuizScore(unitId, score)
        }
    }

    fun resetQuizScores() {
        viewModelScope.launch {
            repo.resetAllScores()
        }
    }
}
