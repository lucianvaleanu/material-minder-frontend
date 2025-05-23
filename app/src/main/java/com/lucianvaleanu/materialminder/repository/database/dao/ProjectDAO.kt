package com.lucianvaleanu.materialminder.repository.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lucianvaleanu.materialminder.model.Project

@Dao
interface ProjectDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<Project>)

    @Query("SELECT * FROM project WHERE userId = :userId")
    suspend fun getAllByUserId(userId: Int): List<Project>

    @Query("DELETE FROM project WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: Int)
}