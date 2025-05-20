package com.lucianvaleanu.materialminder.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lucianvaleanu.materialminder.model.*
import com.lucianvaleanu.materialminder.repository.database.dao.ConstructionItemDAO
import com.lucianvaleanu.materialminder.repository.database.dao.ProjectDAO
import com.lucianvaleanu.materialminder.repository.database.dao.ProjectItemDAO
import com.lucianvaleanu.materialminder.repository.database.dao.UserDAO

@Database(
    entities = [ConstructionItem::class, Project::class, ProjectItem::class, User::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun constructionItemDao(): ConstructionItemDAO
    abstract fun projectDao(): ProjectDAO
    abstract fun projectItemDao(): ProjectItemDAO
    abstract fun userDao(): UserDAO
}