package com.valeanulucian.materialminder.viewmodel

import androidx.lifecycle.ViewModel
import com.valeanulucian.materialminder.model.ConstructionItem
import com.valeanulucian.materialminder.repository.IConstructionItemRepository
import com.valeanulucian.materialminder.repository.InMemoryConstructionItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConstructionItemViewModel(
    private val repository: IConstructionItemRepository = InMemoryConstructionItemRepository()
) : ViewModel() {

    private val _constructionObjects = MutableStateFlow<List<ConstructionItem>>(emptyList())
    val constructionObjects: StateFlow<List<ConstructionItem>> = _constructionObjects

    init {
        loadConstructionObjects()
    }

    private fun loadConstructionObjects() {
        _constructionObjects.value = repository.getAll()
    }

    fun addConstructionObject(constructionItem: ConstructionItem) {
        constructionItem.id = getFirstFreeID()
        repository.add(constructionItem)
        loadConstructionObjects()
    }

    fun updateConstructionObject(constructionItem: ConstructionItem) {
        repository.update(constructionItem)
        loadConstructionObjects()
    }

    fun deleteConstructionObjectById(id: Int) {
        repository.deleteById(id)
        loadConstructionObjects()
    }

    fun getConstructionObjectById(id: Int): ConstructionItem? {
        return repository.getById(id)
    }

    private fun getFirstFreeID(): Int {
        val ids = repository.getAll().map { it.id }.toSet()
        var freeId = 1
        while (freeId in ids) {
            freeId++
        }
        return freeId
    }
}
