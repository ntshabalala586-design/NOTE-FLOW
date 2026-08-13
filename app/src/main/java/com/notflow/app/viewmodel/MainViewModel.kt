package com.notflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.notflow.app.data.model.NoteEntity
import com.notflow.app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    private val _isQuickAddOpen = MutableStateFlow(false)
    val isQuickAddOpen: StateFlow<Boolean> = _isQuickAddOpen.asStateFlow()

    val recentNotes: StateFlow<List<NoteEntity>> = repository.getRecentNotes(limit = 3).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val remainingGroceryCount = repository.remainingGroceryCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val upcomingTrips = repository.getUpcomingShoppingTrips(limit = 3).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val upcomingReminders = repository.getUpcomingReminders(limit = 5).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setGlobalSearchQuery(query: String) {
        _globalSearchQuery.value = query
    }

    fun openQuickAdd() {
        _isQuickAddOpen.value = true
    }

    fun closeQuickAdd() {
        _isQuickAddOpen.value = false
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}
