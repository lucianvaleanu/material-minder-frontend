package com.lucianvaleanu.materialminder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucianvaleanu.materialminder.model.ConstructionItem
import com.lucianvaleanu.materialminder.repository.ConstructionItemRepository
import com.lucianvaleanu.materialminder.service.api.ConstructionItemApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConstructionItemViewModel @Inject constructor(
    private val repository: ConstructionItemRepository,
    private val apiService: ConstructionItemApiService
) : ViewModel() {

    private val _constructionItems = MutableStateFlow<List<ConstructionItem>>(emptyList())
    val constructionItems: StateFlow<List<ConstructionItem>> = _constructionItems.asStateFlow()

    init {
        loadConstructionItems()
    }

    private fun loadConstructionItems() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val items = repository.getAll()
                if (items.isEmpty()) {
                    loadItemsFromApi()
                } else {
                    _constructionItems.value = items
                }
            } catch (e: Exception) {
                Log.e("ConstructionItemViewModel", "Error loading items", e)
            }
        }
    }

    private suspend fun loadItemsFromApi() {
        try {
            val items = apiService.getAllConstructionItems()
            repository.insertAll(items)
            _constructionItems.value = items
        } catch (e: Exception) {
            Log.e("ConstructionItemViewModel", "Error fetching items from API", e)
        }
    }

    fun insertItems(items: List<ConstructionItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertAll(items)
                _constructionItems.value = repository.getAll()
            } catch (e: Exception) {
                Log.e("ConstructionItemViewModel", "Error inserting items", e)
            }
        }
    }

    fun deleteItemById(idToDelete: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteById(idToDelete)
                _constructionItems.value = repository.getAll()
            } catch (e: Exception) {
                Log.e("ConstructionItemViewModel", "Error deleting item", e)
            }
        }
    }

    fun updateItem(updatedObject: ConstructionItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateItem(updatedObject)
                _constructionItems.value = repository.getAll()
            } catch (e: Exception) {
                Log.e("ConstructionItemViewModel", "Error updating item", e)
            }
        }
    }

    suspend fun getItemById(id: Int): ConstructionItem? {
        return try {
            repository.getConstructionItemById(id)
        } catch (e: Exception) {
            Log.e("ConstructionItemViewModel", "Error fetching item by ID", e)
            null
        }
    }
}