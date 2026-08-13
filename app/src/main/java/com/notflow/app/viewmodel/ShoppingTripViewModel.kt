package com.notflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.notflow.app.data.model.ShoppingTripEntity
import com.notflow.app.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingTripViewModel(private val repository: AppRepository) : ViewModel() {

    val shoppingTrips: StateFlow<List<ShoppingTripEntity>> = repository.allShoppingTrips.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val upcomingTrips: StateFlow<List<ShoppingTripEntity>> = repository.getUpcomingShoppingTrips(limit = 3).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveShoppingTrip(
        id: Long = 0,
        title: String,
        date: Long,
        timeHour: Int,
        timeMinute: Int,
        description: String = "",
        reminderMinutesBefore: Int = 30,
        isReminderEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                val trip = ShoppingTripEntity(
                    id = id,
                    title = title.trim(),
                    date = date,
                    timeHour = timeHour,
                    timeMinute = timeMinute,
                    description = description,
                    reminderMinutesBefore = reminderMinutesBefore,
                    isReminderEnabled = isReminderEnabled
                )
                repository.saveShoppingTrip(trip)
            }
        }
    }

    fun deleteShoppingTrip(trip: ShoppingTripEntity) {
        viewModelScope.launch {
            repository.deleteShoppingTrip(trip)
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ShoppingTripViewModel(repository) as T
        }
    }
}
