package com.valeanulucian.materialminder.viewmodel

import androidx.lifecycle.ViewModel
import com.valeanulucian.materialminder.model.Project
import com.valeanulucian.materialminder.repository.IProjectsRepository
import com.valeanulucian.materialminder.repository.InMemoryProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ProjectViewModel(
    private val repository: IProjectsRepository = InMemoryProjectsRepository()
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    init{
        loadProjects()
    }

    private fun loadProjects(){
        _projects.update { repository.getAllProjects() }
    }

    fun addProject(project: Project) {
        project.id = getFirstFreeID()
        repository.addProject(project)
        _projects.update { it + project }
    }

    fun getProject(id: Int): Project? {
        return repository.getProjectById(id)
    }
    fun updateProject(project: Project) {
        repository.updateProject(project)
        _projects.update { it.map { if (it.id == project.id) project else it } }
    }


    fun deleteProject(id: Int) {
        repository.deleteProjectByID(id)
        _projects.update { it.filterNot { it.id == id } }
    }

    private fun getFirstFreeID(): Int {
        val ids = repository.getAllProjects().map{it.id}.toSet()
        var freeId = 1
        while (freeId in ids) {
            freeId++
        }
        return freeId
    }

}