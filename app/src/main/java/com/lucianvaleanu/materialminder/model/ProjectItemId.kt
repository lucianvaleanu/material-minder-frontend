package com.lucianvaleanu.materialminder.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "project_item",
    primaryKeys = ["projectId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ConstructionItem::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProjectItem(
    var projectId: Int,
    val itemId: Int,
    val quantity: Int
)