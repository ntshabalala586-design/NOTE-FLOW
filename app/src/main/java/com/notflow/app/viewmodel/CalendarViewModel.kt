package com.notflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.notflow.app.data.model.CalendarTaskEntity
import com.notflow.app.data.model.ShoppingTripEntity
import com.notflow.app.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarViewModel(private val repository: AppRepository) : ViewModel() {

    // Current Year and Month (Calendar instance)
    private val _currentCalendar = MutableStateFlow(Calendar.getInstance())
    val currentCalendar: StateFlow<Calendar> = _currentCalendar.asStateFlow()

    // Selected Date timestamp (Start of Day)
    private val _selectedDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    val allCalendarTasks = repository.allCalendarTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allShoppingTrips = repository.allShoppingTrips.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDateTasks: StateFlow<List<CalendarTaskEntity>> = _selectedDate.flatMapLatest { dateMillis ->
        val startOfDay = getStartOfDay(dateMillis)
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000L)
        repository.getTasksByDate(startOfDay, endOfDay)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDateShoppingTrips: StateFlow<List<ShoppingTripEntity>> = _selectedDate.flatMapLatest { dateMillis ->
        val startOfDay = getStartOfDay(dateMillis)
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000L)
        repository.getShoppingTripsByDate(startOfDay, endOfDay)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSelectedDate(dateMillis: Long) {
        _selectedDate.value = getStartOfDay(dateMillis)
    }

    fun previousMonth() {
        val cal = _currentCalendar.value.clone() as Calendar
        cal.add(Calendar.MONTH, -1)
        _currentCalendar.value = cal
    }

    fun nextMonth() {
        val cal = _currentCalendar.value.clone() as Calendar
        cal.add(Calendar.MONTH, 1)
        _currentCalendar.value = cal
    }

    fun saveCalendarTask(
        id: Long = 0,
        title: String,
        description: String,
        date: Long,
        timeHour: Int,
        timeMinute: Int,
        reminderMinutesBefore: Int = 30,
        isReminderEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                val task = CalendarTaskEntity(
                    id = id,
                    title = title.trim(),
                    description = description,
                    date = getStartOfDay(date),
                    timeHour = timeHour,
                    timeMinute = timeMinute,
                    reminderMinutesBefore = reminderMinutesBefore,
                    isReminderEnabled = isReminderEnabled
                )
                repository.saveCalendarTask(task)
            }
        }
    }

    fun deleteCalendarTask(task: CalendarTaskEntity) {
        viewModelScope.launch {
            repository.deleteCalendarTask(task)
        }
    }

    private fun getStartOfDay(timeMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(repository) as T
        }
    }
}
