package com.lucianvaleanu.materialminder.repository

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

}