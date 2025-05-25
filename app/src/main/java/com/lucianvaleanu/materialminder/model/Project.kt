package com.lucianvaleanu.materialminder.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "project")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val title: String,
    val projectDate: LocalDate,
    val userId: Int
)