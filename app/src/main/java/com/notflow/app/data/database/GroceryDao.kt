package com.notflow.app.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notflow.app.data.model.GroceryCategoryEntity
import com.notflow.app.data.model.GroceryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_items ORDER BY isChecked ASC, category ASC, id DESC")
    fun getAllGroceryItems(): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE category = :category ORDER BY isChecked ASC, id DESC")
    fun getGroceryItemsByCategory(category: String): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE name LIKE '%' || :query || '%' ORDER BY isChecked ASC")
    fun searchGroceryItems(query: String): Flow<List<GroceryItemEntity>>

    @Query("SELECT COUNT(*) FROM grocery_items WHERE isChecked = 0")
    fun getRemainingItemCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItem(item: GroceryItemEntity): Long

    @Update
    suspend fun updateGroceryItem(item: GroceryItemEntity)

    @Delete
    suspend fun deleteGroceryItem(item: GroceryItemEntity)

    @Query("DELETE FROM grocery_items WHERE id = :id")
    suspend fun deleteGroceryItemById(id: Long)

    @Query("DELETE FROM grocery_items WHERE isChecked = 1")
    suspend fun clearCompletedGroceryItems()

    // Grocery Categories
    @Query("SELECT * FROM grocery_categories ORDER BY displayOrder ASC, name ASC")
    fun getAllGroceryCategories(): Flow<List<GroceryCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGroceryCategory(category: GroceryCategoryEntity): Long

    @Update
    suspend fun updateGroceryCategory(category: GroceryCategoryEntity)

    @Delete
    suspend fun deleteGroceryCategory(category: GroceryCategoryEntity)

    @Query("DELETE FROM grocery_categories WHERE id = :id")
    suspend fun deleteGroceryCategoryById(id: Long)

    @Query("UPDATE grocery_items SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateItemsCategoryName(oldCategory: String, newCategory: String)
}
