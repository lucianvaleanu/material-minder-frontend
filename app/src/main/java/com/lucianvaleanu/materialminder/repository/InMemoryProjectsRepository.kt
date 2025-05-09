package com.valeanulucian.materialminder.repository

import com.valeanulucian.materialminder.model.Project

class InMemoryProjectsRepository : IProjectsRepository {
    private val projects = mutableListOf<Project>()

    override fun addProject(project: Project): Boolean {
        return projects.add(project)
    }

    override fun getProjectById(id: Int): Project? {
        return projects.find { it.id == id }
    }

    override fun getAllProjects(): List<Project> {
        return projects.toList()
    }

    override fun updateProject(project: Project): Boolean {
        val index = projects.indexOfFirst { it.id == project.id }
        return if (index != -1) {
            projects[index] = project
            true
        } else {
            false
        }
    }

    override fun deleteProject(project: Project): Boolean {
        return projects.remove(project)
    }

    override fun deleteProjectByID(id: Int): Boolean {
        return projects.removeIf { it.id == id }
    }
}