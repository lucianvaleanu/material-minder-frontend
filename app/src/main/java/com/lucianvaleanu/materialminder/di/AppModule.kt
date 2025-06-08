package com.lucianvaleanu.materialminder.di

import android.content.Context
import androidx.room.Room
import com.lucianvaleanu.materialminder.model.User
import com.lucianvaleanu.materialminder.repository.ConstructionItemRepository
import com.lucianvaleanu.materialminder.repository.ProjectRepository
import com.lucianvaleanu.materialminder.repository.database.AppDatabase
import com.lucianvaleanu.materialminder.repository.database.dao.ConstructionItemDAO
import com.lucianvaleanu.materialminder.repository.database.dao.ProjectDAO
import com.lucianvaleanu.materialminder.repository.database.dao.ProjectItemDAO
import com.lucianvaleanu.materialminder.service.api.ConstructionItemApiService
import com.lucianvaleanu.materialminder.service.api.ProjectApiService
import com.lucianvaleanu.materialminder.service.api.RetrofitInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "material_minder_db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideConstructionItemDAO(database: AppDatabase): ConstructionItemDAO {
        return database.constructionItemDao()
    }

    @Provides
    @Singleton
    fun provideConstructionItemApiService(): ConstructionItemApiService {
        return RetrofitInstance.constructionItemApi
    }

    @Provides
    @Singleton
    fun provideConstructionItemRepository(
        constructionItemDAO: ConstructionItemDAO
    ): ConstructionItemRepository {
        return ConstructionItemRepository(constructionItemDAO)
    }

    @Provides
    @Singleton
    fun provideProjectDAO(database: AppDatabase): ProjectDAO {
        return database.projectDao()
    }

    @Provides
    @Singleton
    fun provideProjectItemDAO(database: AppDatabase): ProjectItemDAO {
        return database.projectItemDao()
    }

    @Provides
    @Singleton
    fun provideProjectRepository(
        projectDAO: ProjectDAO,
        projectItemDAO: ProjectItemDAO
    ): ProjectRepository {
        return ProjectRepository(projectDAO, projectItemDAO)
    }

    @Provides
    @Singleton
    fun provideDefaultUser(): User {
        return User(
            id = 1,
            email = "user@email.com",
            password = "pass",
            createdAt = Instant.now()
        )
    }

    @Provides
    @Singleton
    fun provideProjectApiService(): ProjectApiService {
        return RetrofitInstance.projectApi
    }
}