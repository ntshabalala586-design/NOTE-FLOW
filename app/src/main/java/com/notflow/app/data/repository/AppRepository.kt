package com.notflow.app.data.repository

import android.content.Context
import com.notflow.app.data.database.AppDatabase
import com.notflow.app.data.model.CalendarTaskEntity
import com.notflow.app.data.model.GroceryCategoryEntity
import com.notflow.app.data.model.GroceryItemEntity
import com.notflow.app.data.model.NoteCategoryEntity
import com.notflow.app.data.model.NoteEntity
import com.notflow.app.data.model.ReminderEntity
import com.notflow.app.data.model.ShoppingTripEntity
import com.notflow.app.notifications.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class AppRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val noteDao = db.noteDao()
    private val groceryDao = db.groceryDao()
    private val shoppingTripDao = db.shoppingTripDao()
    private val reminderDao = db.reminderDao()
    private val calendarTaskDao = db.calendarTaskDao()

    private val alarmScheduler = AlarmScheduler(context)

    // NOTES
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val noteCategories: Flow<List<NoteCategoryEntity>> = noteDao.getAllNoteCategories()
    fun getNotesByCategory(category: String): Flow<List<NoteEntity>> = noteDao.getNotesByCategory(category)
    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)
    fun getRecentNotes(limit: Int = 5): Flow<List<NoteEntity>> = noteDao.getRecentNotes(limit)
    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)
    suspend fun insertNoteCategory(name: String) = noteDao.insertNoteCategory(NoteCategoryEntity(name = name))
    suspend fun deleteNoteCategory(name: String) = noteDao.deleteNoteCategoryByName(name)

    // GROCERY
    val allGroceryItems: Flow<List<GroceryItemEntity>> = groceryDao.getAllGroceryItems()
    val groceryCategories: Flow<List<GroceryCategoryEntity>> = groceryDao.getAllGroceryCategories()
    val remainingGroceryCount: Flow<Int> = groceryDao.getRemainingItemCount()

    fun getGroceryItemsByCategory(category: String): Flow<List<GroceryItemEntity>> = groceryDao.getGroceryItemsByCategory(category)
    fun searchGroceryItems(query: String): Flow<List<GroceryItemEntity>> = groceryDao.searchGroceryItems(query)

    suspend fun insertGroceryItem(item: GroceryItemEntity): Long = groceryDao.insertGroceryItem(item)
    suspend fun updateGroceryItem(item: GroceryItemEntity) = groceryDao.updateGroceryItem(item)
    suspend fun deleteGroceryItem(item: GroceryItemEntity) = groceryDao.deleteGroceryItem(item)
    suspend fun clearCompletedGroceryItems() = groceryDao.clearCompletedGroceryItems()

    suspend fun insertGroceryCategory(name: String, order: Int = 0) {
        groceryDao.insertGroceryCategory(GroceryCategoryEntity(name = name, displayOrder = order))
    }
    suspend fun updateGroceryCategory(category: GroceryCategoryEntity) = groceryDao.updateGroceryCategory(category)
    suspend fun deleteGroceryCategory(category: GroceryCategoryEntity) = groceryDao.deleteGroceryCategory(category)
    suspend fun renameGroceryCategory(oldName: String, newCategory: GroceryCategoryEntity) {
        groceryDao.updateGroceryCategory(newCategory)
        groceryDao.updateItemsCategoryName(oldName, newCategory.name)
    }

    // SHOPPING TRIPS
    val allShoppingTrips: Flow<List<ShoppingTripEntity>> = shoppingTripDao.getAllShoppingTrips()
    fun getUpcomingShoppingTrips(nowMillis: Long = System.currentTimeMillis(), limit: Int = 3) =
        shoppingTripDao.getUpcomingShoppingTrips(nowMillis, limit)
    fun getShoppingTripsByDate(startOfDay: Long, endOfDay: Long) =
        shoppingTripDao.getShoppingTripsByDate(startOfDay, endOfDay)
    suspend fun getShoppingTripById(id: Long) = shoppingTripDao.getShoppingTripById(id)

    suspend fun saveShoppingTrip(trip: ShoppingTripEntity): Long {
        val tripId = shoppingTripDao.insertShoppingTrip(trip)
        val savedTrip = trip.copy(id = if (trip.id == 0L) tripId else trip.id)

        // Schedule notification if reminder enabled
        if (savedTrip.isReminderEnabled && savedTrip.reminderMinutesBefore >= 0) {
            val tripTimeMillis = calculateTripTimeMillis(savedTrip.date, savedTrip.timeHour, savedTrip.timeMinute)
            val reminderTime = tripTimeMillis - (savedTrip.reminderMinutesBefore * 60 * 1000L)

            if (reminderTime > System.currentTimeMillis()) {
                val reminder = ReminderEntity(
                    title = "Shopping Trip: ${savedTrip.title}",
                    description = if (savedTrip.description.isNotEmpty()) savedTrip.description else "Scheduled for today at ${formatTime(savedTrip.timeHour, savedTrip.timeMinute)}",
                    scheduledTime = reminderTime,
                    targetType = "SHOPPING_TRIP",
                    targetId = savedTrip.id
                )
                val remId = reminderDao.insertReminder(reminder)
                alarmScheduler.scheduleAlarm(remId, reminder.title, reminder.description, reminderTime)
            }
        }
        return tripId
    }

    suspend fun deleteShoppingTrip(trip: ShoppingTripEntity) {
        shoppingTripDao.deleteShoppingTrip(trip)
    }

    // REMINDERS
    val activeReminders: Flow<List<ReminderEntity>> = reminderDao.getAllActiveReminders()
    fun getUpcomingReminders(nowMillis: Long = System.currentTimeMillis(), limit: Int = 5) =
        reminderDao.getUpcomingReminders(nowMillis, limit)

    suspend fun saveReminder(reminder: ReminderEntity): Long {
        val remId = reminderDao.insertReminder(reminder)
        alarmScheduler.scheduleAlarm(remId, reminder.title, reminder.description, reminder.scheduledTime)
        return remId
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        alarmScheduler.cancelAlarm(reminder.id)
        reminderDao.deleteReminder(reminder)
    }

    // CALENDAR TASKS
    val allCalendarTasks: Flow<List<CalendarTaskEntity>> = calendarTaskDao.getAllCalendarTasks()
    fun getTasksByDate(startOfDay: Long, endOfDay: Long) = calendarTaskDao.getTasksByDate(startOfDay, endOfDay)
    fun searchCalendarTasks(query: String) = calendarTaskDao.searchCalendarTasks(query)

    suspend fun saveCalendarTask(task: CalendarTaskEntity): Long {
        val taskId = calendarTaskDao.insertCalendarTask(task)
        val savedTask = task.copy(id = if (task.id == 0L) taskId else task.id)

        if (savedTask.isReminderEnabled && savedTask.reminderMinutesBefore >= 0) {
            val taskTimeMillis = calculateTripTimeMillis(savedTask.date, savedTask.timeHour, savedTask.timeMinute)
            val reminderTime = taskTimeMillis - (savedTask.reminderMinutesBefore * 60 * 1000L)

            if (reminderTime > System.currentTimeMillis()) {
                val reminder = ReminderEntity(
                    title = "Reminder: ${savedTask.title}",
                    description = savedTask.description,
                    scheduledTime = reminderTime,
                    targetType = "CALENDAR_TASK",
                    targetId = savedTask.id
                )
                val remId = reminderDao.insertReminder(reminder)
                alarmScheduler.scheduleAlarm(remId, reminder.title, reminder.description, reminderTime)
            }
        }
        return taskId
    }

    suspend fun deleteCalendarTask(task: CalendarTaskEntity) {
        calendarTaskDao.deleteCalendarTask(task)
    }

    private fun calculateTripTimeMillis(dateMillis: Long, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val hour12 = if (hour % 12 == 0) 12 else hour % 12
        return String.format("%d:%02d %s", hour12, minute, amPm)
    }
}
