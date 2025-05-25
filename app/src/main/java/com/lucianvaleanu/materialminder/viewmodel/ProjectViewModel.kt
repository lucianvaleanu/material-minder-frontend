package com.lucianvaleanu.materialminder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucianvaleanu.materialminder.model.Project
import com.lucianvaleanu.materialminder.model.ProjectItem
import com.lucianvaleanu.materialminder.model.User
import com.lucianvaleanu.materialminder.repository.ProjectRepository
import com.lucianvaleanu.materialminder.service.api.ProjectApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val apiService: ProjectApiService,
    private val user: User
) : ViewModel(){
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    init{
        loadProjects()
    }

    private fun loadProjects(){
        Log.i("ProjectViewModel", "Loading projects for user: ${user.id}")
        viewModelScope.launch(Dispatchers.IO){
            try{
                val items = repository.getAllProjectsByUserId(user)
                if(items.isEmpty()){
                    loadProjectsFromApi()
                }else{
                    _projects.value = items
                }
            } catch (e: Exception){
            }
        }
    }

    private fun loadProjectsFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val items = apiService.getAllProjectsByUserId(user.id)
                repository.insertProjects(items)
                _projects.value = items
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun insertProjects(items: List<Project>) {
        Log.i("ProjectViewModel", "Inserting projects: $items")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertProjects(items)
                _projects.value = repository.getAllProjectsByUserId(user)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteProjectById(projectId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteProjectById(projectId)
                _projects.value = repository.getAllProjectsByUserId(user)
            } catch (e: Exception) {

            }
        }
    }

    fun getAllProjectsByUserId() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val items = repository.getAllProjectsByUserId(user)
                _projects.value = items
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    suspend fun getAllProjectItemsByProjectId(projectId: Int): List<ProjectItem> {
        return try {
            repository.getAllProjectItemsByProjectId(projectId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getProjectById(projectId: Int): Project? {
        return _projects.value.find { it.id == projectId }
    }

    fun insertProjectItems(selectedItems: List<ProjectItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertProjectItems(selectedItems)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addProjectWithItems(project: Project, items: List<ProjectItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertProjects(listOf(project))
                val projectId = repository.getAllProjectsByUserId(user).last().id
                items.forEach {
                    if (projectId != null) {
                        it.projectId = projectId
                    }
                }
                repository.insertProjectItems(items)
                _projects.value = repository.getAllProjectsByUserId(user)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}