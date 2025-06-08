package com.lucianvaleanu.materialminder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucianvaleanu.materialminder.model.ConstructionItem
import com.lucianvaleanu.materialminder.repository.ConstructionItemRepository
import com.lucianvaleanu.materialminder.service.api.ConstructionItemApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class ConstructionItemViewModel @Inject constructor(
    private val repository: ConstructionItemRepository,
    private val apiService: ConstructionItemApiService
) : ViewModel() {

    private val _currentQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = _currentQuery

    private val _refreshTrigger = MutableStateFlow(0)
    private val _lastProcessedItemNames = MutableStateFlow<List<String>>(emptyList())

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val constructionItems: StateFlow<List<ConstructionItem>> =
        combine(
            _currentQuery,
            _refreshTrigger,
            _lastProcessedItemNames
        ) { query, _, processedNames ->
            Triple(query, processedNames, Unit)
        }
            .debounce(300L)
            .flatMapLatest { (query, processedNames, _) ->
                flow {
                    try {
                        if (query.isBlank()) {
                            var itemsToShow = repository.getAll()

                            if (itemsToShow.isEmpty()) {
                                Log.d("ConstructionItemVM", "DB empty for blank query, fetching from API.")
                                val apiItems = apiService.getAllConstructionItems()
                                if (apiItems.isNotEmpty()) {
                                    repository.insertAll(apiItems)
                                    itemsToShow = repository.getAll()
                                    Log.d("ConstructionItemVM", "Fetched from API and inserted. Total items: ${itemsToShow.size}")
                                } else {
                                    Log.d("ConstructionItemVM", "API also returned empty.")
                                    emit(emptyList())
                                    return@flow
                                }
                            }

                            if (processedNames.isNotEmpty()) {
                                val (priorityItems, otherItems) = itemsToShow.partition { item ->
                                    processedNames.any { processedName ->
                                        item.name.equals(processedName, ignoreCase = true)
                                    }
                                }
                                val sortedPriorityItems = priorityItems.sortedWith(compareBy { item ->
                                    processedNames.indexOfFirst { processedName ->
                                        item.name.equals(processedName, ignoreCase = true)
                                    }
                                })
                                val sortedOtherItems = otherItems.sortedByDescending { it.id }
                                emit(sortedPriorityItems + sortedOtherItems)
                                Log.d("ConstructionItemVM", "Emitting prioritized list. Priority: ${sortedPriorityItems.map { it.name }}, Others (newest first): ${sortedOtherItems.map { it.name }}")
                            } else {
                                emit(itemsToShow.sortedByDescending { it.id })
                                Log.d("ConstructionItemVM", "Emitting all items, sorted by newest first. Count: ${itemsToShow.size}")
                            }
                        } else {
                            val searchResults = repository.searchByName(query)
                            emit(searchResults.sortedBy { it.name })
                            Log.d("ConstructionItemVM", "Emitting search results for '$query', sorted by name. Count: ${searchResults.size}")
                        }
                    } catch (e: Exception) {
                        Log.e("ConstructionItemVM", "Error in constructionItems flow for query '$query', processedNames: '$processedNames'", e)
                        emit(emptyList())
                    }
                }.flowOn(Dispatchers.IO)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )

    fun setSearchQuery(query: String) {
        val trimmedQuery = query.trim()
        _currentQuery.value = trimmedQuery
        if (trimmedQuery.isNotEmpty()) {
            _lastProcessedItemNames.value = emptyList()
        }
    }

    private fun refreshData() {
        _refreshTrigger.value++
    }

    fun insertItems(items: List<ConstructionItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertAll(items)
                refreshData()
            } catch (e: Exception) {
                Log.e("ConstructionItemVM", "Error inserting items", e)
            }
        }
    }

    fun deleteItemById(idToDelete: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteById(idToDelete)
                refreshData()
            } catch (e: Exception) {
                Log.e("ConstructionItemVM", "Error deleting item", e)
            }
        }
    }

    fun updateItem(updatedObject: ConstructionItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateItem(updatedObject)
                refreshData()
            } catch (e: Exception) {
                Log.e("ConstructionItemVM", "Error updating item", e)
            }
        }
    }

    suspend fun getItemById(id: Int): ConstructionItem? {
        return try {
            repository.getConstructionItemById(id)
        } catch (e: Exception) {
            Log.e("ConstructionItemVM", "Error fetching item by ID $id", e)
            null
        }
    }

    fun processSpokenItems(pairs: List<Pair<String, Int>>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (pairs.isEmpty()) {
                if (_lastProcessedItemNames.value.isNotEmpty()){
                    _lastProcessedItemNames.value = emptyList()
                    refreshData()
                }
                return@launch
            }

            val itemNamesToPrioritize = mutableListOf<String>()

            for ((rawItemName, _) in pairs) {
                val itemName = rawItemName.trim()
                if (itemName.isEmpty()) continue

                val existingItem = repository.searchByName(itemName)
                    .find { it.name.equals(itemName, ignoreCase = true) }

                if (existingItem == null) {
                    val newItem = ConstructionItem(
                        name = itemName,
                        price = BigDecimal.ZERO,
                        image = ""
                    )
                    try {
                        repository.insertAll(listOf(newItem))
                        Log.d("ConstructionItemVM", "Created new item: $itemName")
                    } catch (e: Exception) {
                        Log.e("ConstructionItemVM", "Error creating new item: $itemName", e)
                    }
                }

                itemNamesToPrioritize.add(itemName)
            }

            if (_currentQuery.value.isNotBlank()) {
                _currentQuery.value = ""
            }

            _lastProcessedItemNames.value = itemNamesToPrioritize.distinct()
            Log.d("ConstructionItemVM", "Prioritizing items: ${itemNamesToPrioritize.distinct()}")

            delay(100)
            refreshData()
        }
    }

    suspend fun insertAndReturnItems(itemsToCreate: List<ConstructionItem>): List<ConstructionItem> {
        return repository.insertAndReturnItems(itemsToCreate)
    }
}