package com.valeanulucian.materialminder.repository

import com.valeanulucian.materialminder.model.Project

interface IProjectsRepository {
    fun addProject(project: Project): Boolean
    fun getProjectById(id: Int): Project?
    fun getAllProjects(): List<Project>
    fun updateProject(project: Project): Boolean
    fun deleteProject(project: Project): Boolean
    fun deleteProjectByID(id: Int): Boolean
}