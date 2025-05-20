package com.lucianvaleanu.materialminder.repository

import com.lucianvaleanu.materialminder.model.Project
import com.lucianvaleanu.materialminder.model.ProjectItem
import com.lucianvaleanu.materialminder.model.User
import com.lucianvaleanu.materialminder.repository.database.dao.ProjectDAO
import com.lucianvaleanu.materialminder.repository.database.dao.ProjectItemDAO

class ProjectRepository(
    private val projectDAO: ProjectDAO,
    private val projectItemDAO: ProjectItemDAO
) {
    suspend fun getAllProjectsByUserId(user: User): List<Project> {
        return projectDAO.getAllByUserId(user.id)
    }

    suspend fun getAllProjectItemsByProjectId(projectId: Int): List<ProjectItem> {
        return projectItemDAO.getAllByProjectId(projectId)
    }

    suspend fun insertProjects(projects: List<Project>) {
        projectDAO.insertAll(projects)
    }

    suspend fun insertProjectItems(projectItems: List<ProjectItem>) {
        projectItemDAO.insertAll(projectItems)
    }

    suspend fun deleteProjectById(projectId: Int) {
        projectDAO.deleteProjectById(projectId)
    }
}