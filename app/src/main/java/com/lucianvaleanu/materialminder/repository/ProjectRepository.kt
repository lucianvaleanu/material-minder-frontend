package com.lucianvaleanu.materialminder.repository

import com.lucianvaleanu.materialminder.model.Project
import com.lucianvaleanu.materialminder.model.ProjectItem
import com.lucianvaleanu.materialminder.model.User
import com.lucianvaleanu.materialminder.repository.database.dao.ProjectDAO
import com.lucianvaleanu.materialminder.repository.database.dao.ProjectItemDAO
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val projectDAO: ProjectDAO,
    private val projectItemDAO: ProjectItemDAO
) {

    suspend fun getAllProjectsByUserId(user: User): List<Project> {
        return projectDAO.getAllByUserId(user.id)
    }

    suspend fun insertProjects(projects: List<Project>) {
        projectDAO.insertAll(projects)
    }

    suspend fun deleteProjectById(projectId: Int) {
        projectDAO.deleteProjectById(projectId)
        projectItemDAO.deleteAllByProjectId(projectId)
    }

    suspend fun getAllProjectItemsByProjectId(projectId: Int): List<ProjectItem> {
        return projectItemDAO.getAllByProjectId(projectId)
    }

    suspend fun insertProjectItems(projectItems: List<ProjectItem>) {
        projectItemDAO.insertAll(projectItems)
    }

    suspend fun updateProject(project: Project) {
        projectDAO.updateProject(project)
    }

    suspend fun updateProjectItem(projectItem: ProjectItem) {
        projectItemDAO.updateProjectItem(projectItem)
    }

    suspend fun updateProjectItems(projectId: Int, items: List<ProjectItem>) {
        projectItemDAO.deleteAllByProjectId(projectId)
        val itemsWithProjectId = items.map { it.copy(projectId = projectId) }
        projectItemDAO.insertAll(itemsWithProjectId)
    }

    suspend fun insertProjectsAndReturn(projects: List<Project>): List<Project> {
        val returnedProjects = mutableListOf<Project>()
        for (project in projects) {
            val newRowId = projectDAO.insertSingleProjectAndGetId(project)
            val insertedProjectWithId = projectDAO.getProjectById(newRowId.toInt())
            insertedProjectWithId?.let {
                returnedProjects.add(it)
            }
        }
        return returnedProjects
    }
}