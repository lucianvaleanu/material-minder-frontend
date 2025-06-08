package com.lucianvaleanu.materialminder.repository

import android.util.Log
import com.lucianvaleanu.materialminder.model.ConstructionItem
import com.lucianvaleanu.materialminder.repository.database.dao.ConstructionItemDAO

class ConstructionItemRepository(private val constructionItemDAO: ConstructionItemDAO) {

    suspend fun insertAll(items: List<ConstructionItem>) {
        constructionItemDAO.insertAll(items)
    }

    suspend fun getAll(): List<ConstructionItem> {
        return constructionItemDAO.getAll()
    }

    suspend fun getConstructionItemById(id: Int): ConstructionItem {
        return constructionItemDAO.getConstructionItemById(id)
    }

    suspend fun deleteById(id: Int) {
        constructionItemDAO.deleteById(id)
    }

    suspend fun updateItem(item: ConstructionItem) {
        constructionItemDAO.updateItem(item)
    }

    suspend fun searchByName(query: String): List<ConstructionItem> {
        return constructionItemDAO.searchByName(query)
    }

    suspend fun insertAndReturnItems(items: List<ConstructionItem>): List<ConstructionItem> {
        if (items.isEmpty()) return emptyList()
        val itemsWithIds = mutableListOf<ConstructionItem>()
        for (item in items) {
            val newItemToInsert = if (item.id != null) item.copy(id = null) else item
            val newId = constructionItemDAO.insertOneAndGetId(newItemToInsert)
            if (newId != -1L) {
                itemsWithIds.add(item.copy(id = newId.toInt(), name = newItemToInsert.name, price = newItemToInsert.price, image = newItemToInsert.image))
            } else {
                Log.w("ConstructionItemRepo", "Failed to insert item or it already exists: ${item.name}")
            }
        }
        return itemsWithIds
    }

}