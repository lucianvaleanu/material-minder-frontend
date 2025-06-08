package com.lucianvaleanu.materialminder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucianvaleanu.materialminder.model.ConstructionItem
import com.lucianvaleanu.materialminder.model.Project
import com.lucianvaleanu.materialminder.model.ProjectItem
import com.lucianvaleanu.materialminder.model.ProjectItemDraft
import com.lucianvaleanu.materialminder.model.User
import com.lucianvaleanu.materialminder.repository.ProjectRepository
import com.lucianvaleanu.materialminder.service.api.ProjectApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
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
                Log.e("ProjectViewModel", "Error loading projects", e)
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
                Log.e("ProjectViewModel", "Error loading projects from API", e)
            }
        }
    }

    fun deleteProjectById(projectId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteProjectById(projectId)
                _projects.value = repository.getAllProjectsByUserId(user)
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error deleting project by ID", e)
            }
        }
    }

    suspend fun getAllProjectItemsByProjectId(projectId: Int): List<ProjectItem> {
        return try {
            repository.getAllProjectItemsByProjectId(projectId)
        } catch (e: Exception) {
            Log.e("ProjectViewModel", "Error getting all project items by project ID", e)
            emptyList()
        }
    }

    fun getProjectById(projectId: Int): Project? {
        return _projects.value.find { it.id == projectId }
    }

    fun addProjectWithItems(project: Project, items: List<ProjectItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val insertedProjects = repository.insertProjectsAndReturn(listOf(project))
                val newProject = insertedProjects.firstOrNull()

                if (newProject?.id != null) {
                    val itemsWithProjectId = items.map { it.copy(projectId = newProject.id) }
                    repository.insertProjectItems(itemsWithProjectId)
                    _projects.value = repository.getAllProjectsByUserId(user)
                } else {
                    repository.insertProjects(listOf(project))
                    val currentProjects = repository.getAllProjectsByUserId(user)
                    val projectId = currentProjects.find { it.title == project.title && it.userId == user.id }?.id
                        ?: currentProjects.lastOrNull()?.id

                    if (projectId != null) {
                        val itemsWithProjectId = items.map { it.copy(projectId = projectId) }
                        repository.insertProjectItems(itemsWithProjectId)
                        _projects.value = repository.getAllProjectsByUserId(user)
                    } else {
                        Log.e("ProjectViewModel", "Failed to get project ID for new project in addProjectWithItems")
                    }
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error adding project with items", e)
            }
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateProject(project)
                _projects.value = repository.getAllProjectsByUserId(user)
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error updating project", e)
            }
        }
    }

    fun processProjectItemDraftsAndUpdateItems(
        projectId: Int,
        drafts: List<ProjectItemDraft>,
        constructionItemViewModel: ConstructionItemViewModel
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("ProjectViewModel", "Processing drafts for projectId $projectId: $drafts")
                val newConstructionItemDrafts = drafts.filter { it.itemIdentifier is String && it.quantity > 0 }
                val newConstructionItemsToCreate = newConstructionItemDrafts.map { draft ->
                    ConstructionItem(
                        name = draft.itemIdentifier as String,
                        price = BigDecimal.ZERO,
                        image = ""
                    )
                }

                val persistedNewItemsWithIds = if (newConstructionItemsToCreate.isNotEmpty()) {
                    Log.d("ProjectViewModel", "Inserting new construction items: $newConstructionItemsToCreate")
                    constructionItemViewModel.insertAndReturnItems(newConstructionItemsToCreate)
                } else {
                    emptyList()
                }
                Log.d("ProjectViewModel", "Persisted new items with IDs: $persistedNewItemsWithIds")


                val finalProjectItems = mutableListOf<ProjectItem>()
                drafts.forEach { draft ->
                    if (draft.quantity <= 0) return@forEach

                    val itemId: Int? = when (val identifier = draft.itemIdentifier) {
                        is Int -> identifier
                        is String -> {
                            persistedNewItemsWithIds.find { ci -> ci.name.equals(identifier, ignoreCase = true) }?.id
                                ?: run {
                                    Log.w("ProjectViewModel", "Could not find ID for new item draft: $identifier")
                                    null
                                }
                        }
                        else -> {
                            Log.w("ProjectViewModel", "Unknown itemIdentifier type: ${identifier.javaClass.name}")
                            null
                        }
                    }

                    itemId?.let {
                        finalProjectItems.add(
                            ProjectItem(
                                projectId = projectId,
                                itemId = it,
                                quantity = draft.quantity
                            )
                        )
                    }
                }
                Log.d("ProjectViewModel", "Final project items to update for projectId $projectId: $finalProjectItems")
                repository.updateProjectItems(projectId, finalProjectItems)
                Log.d("ProjectViewModel", "Successfully updated project items for projectId $projectId")

            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error processing project item drafts for project $projectId", e)
            }
        }
    }
}