package com.notflow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_trips")
data class ShoppingTripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: Long, // timestamp for date
    val timeHour: Int, // 0-23
    val timeMinute: Int, // 0-59
    val description: String = "",
    val reminderMinutesBefore: Int = 30, // 0, 15, 30, 60, 1440, or -1 for none
    val isReminderEnabled: Boolean = true
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val scheduledTime: Long, // timestamp
    val isTriggered: Boolean = false,
    val targetType: String = "GENERAL", // GENERAL, SHOPPING_TRIP, CALENDAR_TASK
    val targetId: Long? = null
)

@Entity(tableName = "calendar_tasks")
data class CalendarTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: Long, // Timestamp normalized to start of day
    val timeHour: Int = 10,
    val timeMinute: Int = 0,
    val reminderMinutesBefore: Int = 30,
    val isReminderEnabled: Boolean = true,
    val taskType: String = "TASK" // TASK, SHOPPING, NOTE
)
