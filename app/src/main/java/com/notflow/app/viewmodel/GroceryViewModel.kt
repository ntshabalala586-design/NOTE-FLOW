package com.notflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.notflow.app.data.model.GroceryCategoryEntity
import com.notflow.app.data.model.GroceryItemEntity
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

class GroceryViewModel(private val repository: AppRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val categories = repository.groceryCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val remainingCount = repository.remainingGroceryCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val groceryItems: StateFlow<List<GroceryItemEntity>> = combine(_selectedCategory, _searchQuery) { cat, query ->
        Pair(cat, query)
    }.flatMapLatest { (cat, query) ->
        if (query.isNotBlank()) {
            repository.searchGroceryItems(query)
        } else if (cat != null && cat != "All") {
            repository.getGroceryItemsByCategory(cat)
        } else {
            repository.allGroceryItems
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleItemCheck(item: GroceryItemEntity) {
        viewModelScope.launch {
            repository.updateGroceryItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun saveGroceryItem(
        id: Long = 0,
        name: String,
        category: String,
        isChecked: Boolean = false
    ) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val item = GroceryItemEntity(
                    id = id,
                    name = name.trim(),
                    category = category.ifBlank { "Other" },
                    isChecked = isChecked
                )
                if (id == 0L) {
                    repository.insertGroceryItem(item)
                } else {
                    repository.updateGroceryItem(item)
                }
            }
        }
    }

    fun deleteGroceryItem(item: GroceryItemEntity) {
        viewModelScope.launch {
            repository.deleteGroceryItem(item)
        }
    }

    fun clearCompletedItems() {
        viewModelScope.launch {
            repository.clearCompletedGroceryItems()
        }
    }

    fun addCategory(categoryName: String) {
        viewModelScope.launch {
            if (categoryName.isNotBlank()) {
                repository.insertGroceryCategory(categoryName.trim())
            }
        }
    }

    fun renameCategory(category: GroceryCategoryEntity, newName: String) {
        viewModelScope.launch {
            if (newName.isNotBlank() && newName != category.name) {
                repository.renameGroceryCategory(category.name, category.copy(name = newName.trim()))
            }
        }
    }

    fun deleteCategory(category: GroceryCategoryEntity) {
        viewModelScope.launch {
            repository.deleteGroceryCategory(category)
        }
    }

    fun moveItemToCategory(item: GroceryItemEntity, newCategory: String) {
        viewModelScope.launch {
            repository.updateGroceryItem(item.copy(category = newCategory))
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GroceryViewModel(repository) as T
        }
    }
}
