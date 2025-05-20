package com.lucianvaleanu.materialminder.repository.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lucianvaleanu.materialminder.model.ProjectItem

@Dao
interface ProjectItemDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projectItems: List<ProjectItem>)

    @Query("SELECT * FROM project_item")
    suspend fun getAll(): List<ProjectItem>

    @Query("SELECT * FROM project_item WHERE projectId = :projectId")
    suspend fun getAllByProjectId(projectId: Int): List<ProjectItem>
}