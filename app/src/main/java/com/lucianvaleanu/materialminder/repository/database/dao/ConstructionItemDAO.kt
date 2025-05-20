package com.lucianvaleanu.materialminder.repository.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lucianvaleanu.materialminder.model.ConstructionItem

@Dao
interface ConstructionItemDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ConstructionItem>)

    @Query("SELECT * FROM construction_item")
    suspend fun getAll(): List<ConstructionItem>

    @Query("SELECT * FROM construction_item WHERE id = :id")
    suspend fun getConstructionItemById(id: Int): ConstructionItem

    @Query("DELETE FROM construction_item WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateItem(item: ConstructionItem)
}