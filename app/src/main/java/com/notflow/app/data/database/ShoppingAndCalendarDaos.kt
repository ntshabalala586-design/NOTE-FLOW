package com.notflow.app.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notflow.app.data.model.CalendarTaskEntity
import com.notflow.app.data.model.ReminderEntity
import com.notflow.app.data.model.ShoppingTripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingTripDao {
    @Query("SELECT * FROM shopping_trips ORDER BY date ASC, timeHour ASC, timeMinute ASC")
    fun getAllShoppingTrips(): Flow<List<ShoppingTripEntity>>

    @Query("SELECT * FROM shopping_trips WHERE date >= :nowMillis ORDER BY date ASC, timeHour ASC, timeMinute ASC LIMIT :limit")
    fun getUpcomingShoppingTrips(nowMillis: Long, limit: Int = 3): Flow<List<ShoppingTripEntity>>

    @Query("SELECT * FROM shopping_trips WHERE id = :id")
    suspend fun getShoppingTripById(id: Long): ShoppingTripEntity?

    @Query("SELECT * FROM shopping_trips WHERE date >= :startOfDay AND date < :endOfDay ORDER BY timeHour ASC, timeMinute ASC")
    fun getShoppingTripsByDate(startOfDay: Long, endOfDay: Long): Flow<List<ShoppingTripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingTrip(trip: ShoppingTripEntity): Long

    @Update
    suspend fun updateShoppingTrip(trip: ShoppingTripEntity)

    @Delete
    suspend fun deleteShoppingTrip(trip: ShoppingTripEntity)

    @Query("DELETE FROM shopping_trips WHERE id = :id")
    suspend fun deleteShoppingTripById(id: Long)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isTriggered = 0 ORDER BY scheduledTime ASC")
    fun getAllActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isTriggered = 0 AND scheduledTime >= :nowMillis ORDER BY scheduledTime ASC LIMIT :limit")
    fun getUpcomingReminders(nowMillis: Long, limit: Int = 5): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isTriggered = 0 AND scheduledTime >= :nowMillis ORDER BY scheduledTime ASC")
    suspend fun getUpcomingRemindersList(nowMillis: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isTriggered = 1 WHERE id = :id")
    suspend fun markReminderTriggered(id: Long)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
}

@Dao
interface CalendarTaskDao {
    @Query("SELECT * FROM calendar_tasks ORDER BY date ASC, timeHour ASC, timeMinute ASC")
    fun getAllCalendarTasks(): Flow<List<CalendarTaskEntity>>

    @Query("SELECT * FROM calendar_tasks WHERE date >= :startOfDay AND date < :endOfDay ORDER BY timeHour ASC, timeMinute ASC")
    fun getTasksByDate(startOfDay: Long, endOfDay: Long): Flow<List<CalendarTaskEntity>>

    @Query("SELECT * FROM calendar_tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchCalendarTasks(query: String): Flow<List<CalendarTaskEntity>>

    @Query("SELECT * FROM calendar_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): CalendarTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarTask(task: CalendarTaskEntity): Long

    @Update
    suspend fun updateCalendarTask(task: CalendarTaskEntity)

    @Delete
    suspend fun deleteCalendarTask(task: CalendarTaskEntity)

    @Query("DELETE FROM calendar_tasks WHERE id = :id")
    suspend fun deleteCalendarTaskById(id: Long)
}
