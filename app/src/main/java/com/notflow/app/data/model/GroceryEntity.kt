package com.notflow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val isChecked: Boolean = false,
    val createdDate: Long = System.currentTimeMillis(),
    val tripId: Long? = null
)

@Entity(tableName = "grocery_categories")
data class GroceryCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val displayOrder: Int = 0
)
