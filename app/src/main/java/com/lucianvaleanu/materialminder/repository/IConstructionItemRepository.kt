package com.valeanulucian.materialminder.repository

import com.valeanulucian.materialminder.model.ConstructionItem

interface IConstructionItemRepository {
    fun getAll(): List<ConstructionItem>
    fun getById(id: Int): ConstructionItem?
    fun add(constructionItem: ConstructionItem): Boolean
    fun update(constructionItem: ConstructionItem): Boolean
    fun deleteById(id: Int): Boolean
}