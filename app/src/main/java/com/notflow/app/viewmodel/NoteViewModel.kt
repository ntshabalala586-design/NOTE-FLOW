package com.notflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.notflow.app.data.model.NoteEntity
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

class NoteViewModel(private val repository: AppRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val categories = repository.noteCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<NoteEntity>> = combine(_selectedCategory, _searchQuery) { cat, query ->
        Pair(cat, query)
    }.flatMapLatest { (cat, query) ->
        if (query.isNotBlank()) {
            repository.searchNotes(query)
        } else if (cat != null && cat != "All") {
            repository.getNotesByCategory(cat)
        } else {
            repository.allNotes
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

    fun saveNote(
        id: Long = 0,
        title: String,
        content: String,
        category: String,
        isPinned: Boolean = false
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (id == 0L) {
                val note = NoteEntity(
                    title = title,
                    content = content,
                    category = category.ifBlank { "General" },
                    createdDate = now,
                    updatedDate = now,
                    isPinned = isPinned
                )
                repository.insertNote(note)
            } else {
                val existing = repository.getNoteById(id)
                val note = NoteEntity(
                    id = id,
                    title = title,
                    content = content,
                    category = category.ifBlank { "General" },
                    createdDate = existing?.createdDate ?: now,
                    updatedDate = now,
                    isPinned = isPinned
                )
                repository.updateNote(note)
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun addCategory(categoryName: String) {
        viewModelScope.launch {
            if (categoryName.isNotBlank()) {
                repository.insertNoteCategory(categoryName.trim())
            }
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(repository) as T
        }
    }
}
