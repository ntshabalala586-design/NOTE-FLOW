package com.notflow.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.notflow.app.data.model.CalendarTaskEntity
import com.notflow.app.data.model.GroceryCategoryEntity
import com.notflow.app.data.model.GroceryItemEntity
import com.notflow.app.data.model.NoteCategoryEntity
import com.notflow.app.data.model.NoteEntity
import com.notflow.app.data.model.ReminderEntity
import com.notflow.app.data.model.ShoppingTripEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        NoteEntity::class,
        NoteCategoryEntity::class,
        GroceryItemEntity::class,
        GroceryCategoryEntity::class,
        ShoppingTripEntity::class,
        ReminderEntity::class,
        CalendarTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun groceryDao(): GroceryDao
    abstract fun shoppingTripDao(): ShoppingTripDao
    abstract fun reminderDao(): ReminderDao
    abstract fun calendarTaskDao(): CalendarTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "noteflow_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaults(database)
                    }
                }
            }
        }

        private suspend fun populateDefaults(db: AppDatabase) {
            // Note categories
            val noteCategories = listOf(
                "General", "Personal", "Work", "Shopping", "Home", "Important"
            )
            noteCategories.forEach { name ->
                db.noteDao().insertNoteCategory(NoteCategoryEntity(name = name))
            }

            // Grocery categories
            val groceryCategories = listOf(
                "Produce", "Dairy", "Meat", "Bakery", "Frozen", "Drinks", "Snacks", "Household", "Other"
            )
            groceryCategories.forEachIndexed { index, name ->
                db.groceryDao().insertGroceryCategory(
                    GroceryCategoryEntity(name = name, displayOrder = index)
                )
            }
        }
    }
}
