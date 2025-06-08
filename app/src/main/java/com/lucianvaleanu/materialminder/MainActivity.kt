package com.lucianvaleanu.materialminder

import com.lucianvaleanu.materialminder.ui.components.construction_item.AddConstructionItem
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lucianvaleanu.materialminder.model.ConstructionItem
import com.lucianvaleanu.materialminder.model.ProjectItem
import com.lucianvaleanu.materialminder.model.ProjectItemDraft
import com.lucianvaleanu.materialminder.ui.theme.MaterialMinderTheme
import com.lucianvaleanu.materialminder.ui.components.*
import com.lucianvaleanu.materialminder.ui.components.construction_item.ConstructionItemList
import com.lucianvaleanu.materialminder.ui.components.construction_item.ConstructionItemDetailScreen
import com.lucianvaleanu.materialminder.ui.components.projects.AddProjectScreen
import com.lucianvaleanu.materialminder.ui.components.projects.ChangeConstructionItemsScreen
import com.lucianvaleanu.materialminder.ui.components.projects.ProjectConstructionItemsScreen
import com.lucianvaleanu.materialminder.ui.components.projects.ProjectDetailScreen
import com.lucianvaleanu.materialminder.ui.components.projects.ProjectsList
import com.lucianvaleanu.materialminder.ui.components.projects.SelectConstructionItemsScreen
import com.lucianvaleanu.materialminder.viewmodel.ConstructionItemViewModel
import com.lucianvaleanu.materialminder.viewmodel.ProjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigDecimal

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val constructionItemViewModel: ConstructionItemViewModel by viewModels<ConstructionItemViewModel>()
    private val projectViewModel: ProjectViewModel by viewModels<ProjectViewModel>()

    companion object {
        private var appContext: Context? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appContext = applicationContext
        setContent {
            MaterialMinderTheme {
                MaterialMinder(
                    modifier = Modifier.fillMaxSize(),
                    constructionItemViewModel = constructionItemViewModel,
                    projectViewModel = projectViewModel
                )
            }
        }
    }
}

@Composable
fun MaterialMinder(
    modifier: Modifier = Modifier,
    constructionItemViewModel: ConstructionItemViewModel,
    projectViewModel: ProjectViewModel,
) {
    val navController = rememberNavController()

    val constructionObjectsList by constructionItemViewModel.constructionItems.collectAsState()
    val currentSearchQuery by constructionItemViewModel.currentQuery.collectAsState()
    val projectsList by projectViewModel.projects.collectAsState()
    var shouldShowOnboarding by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()


    Surface(modifier = modifier) {
        if (shouldShowOnboarding) {
            OnboardingScreen(onTimeout = { shouldShowOnboarding = false })
        } else {
            NavHost(navController = navController, startDestination = "projects") {
                composable("projects") {
                    ProjectsList(
                        projectsList = projectsList,
                        navController = navController,
                        onDelete = { project ->
                            project.id?.let { it1 ->
                                projectViewModel.deleteProjectById(
                                    it1
                                )
                            }
                        }
                    )
                }
                composable("addProject") {
                    AddProjectScreen(
                        navController = navController,
                        onAddProject = { project, selectedProjectItems ->
                            projectViewModel.addProjectWithItems(project, selectedProjectItems)
                            navController.popBackStack()
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }

                composable("selectMaterials") {
                    val initialProjectItems = remember { mutableListOf<ProjectItem>() }
                    SelectConstructionItemsScreen(
                        navController = navController,
                        constructionObjectsList = constructionObjectsList,
                        projectItems = initialProjectItems,
                        onCancel = { navController.popBackStack() },
                        onConfirm = { projectItemDrafts ->
                            scope.launch {
                                val newConstructionItemsToCreateInDb = projectItemDrafts
                                    .filter { it.itemIdentifier is String && it.quantity > 0 }
                                    .map { draft ->
                                        ConstructionItem(
                                            name = draft.itemIdentifier as String,
                                            price = BigDecimal.ZERO,
                                            image = ""
                                        )
                                    }

                                val persistedNewItemsWithIds = if (newConstructionItemsToCreateInDb.isNotEmpty()) {
                                    constructionItemViewModel.insertAndReturnItems(newConstructionItemsToCreateInDb)
                                } else {
                                    emptyList()
                                }

                                val finalProjectItems = mutableListOf<ProjectItem>()
                                projectItemDrafts.forEach { draft ->
                                    if (draft.quantity <= 0) return@forEach

                                    val itemId: Int? = when (draft.itemIdentifier) {
                                        is Int -> draft.itemIdentifier
                                        is String -> {
                                            persistedNewItemsWithIds.find { it.name.equals(draft.itemIdentifier, ignoreCase = true) }?.id
                                        }
                                        else -> null
                                    }

                                    itemId?.let {
                                        finalProjectItems.add(
                                            ProjectItem(
                                                projectId = 0,
                                                itemId = it,
                                                quantity = draft.quantity
                                            )
                                        )
                                    }
                                }
                                Log.d("SelectMaterialsConfirm", "Final ProjectItems to SavedStateHandle: $finalProjectItems")
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "selectedMaterials",
                                    ArrayList(finalProjectItems)
                                )
                                navController.popBackStack()
                            }
                        },
                        currentSearchQuery = currentSearchQuery,
                        onSearchQueryChanged = { query ->
                            constructionItemViewModel.setSearchQuery(query)
                        },
                        onSpokenItemsDetected = { pairs ->
                            constructionItemViewModel.processSpokenItems(pairs)
                        }
                    )
                }
                composable(
                    "changeMaterials/{projectId}",
                    arguments = listOf(navArgument("projectId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val projectId = backStackEntry.arguments?.getInt("projectId")
                    val projectInitialItemsState = remember { mutableStateOf<List<ProjectItem>>(emptyList()) }

                    LaunchedEffect(projectId) {
                        if (projectId != null) {
                            projectInitialItemsState.value = projectViewModel.getAllProjectItemsByProjectId(projectId)
                        }
                    }

                    if (projectId != null) {
                        ChangeConstructionItemsScreen(
                            navController = navController,
                            constructionObjectsList = constructionObjectsList,
                            initialProjectItems = projectInitialItemsState.value,
                            onCancel = { navController.popBackStack() },
                            currentSearchQuery = currentSearchQuery,
                            onSearchQueryChanged = { query ->
                                constructionItemViewModel.setSearchQuery(query)
                            }
                        )
                    }
                }
                composable(
                    "projectDetail/{projectId}",
                    arguments = listOf(navArgument("projectId") {
                        type = NavType.IntType
                    })
                ) { backStackEntry ->
                    val projectId = backStackEntry.arguments?.getInt("projectId")
                    val project = projectId?.let { projectViewModel.getProjectById(it) }

                    val updatedItemDraftsFromHandle = backStackEntry.savedStateHandle
                        .getLiveData<ArrayList<ProjectItemDraft>>("updatedProjectItemDrafts")
                        .observeAsState()

                    LaunchedEffect(updatedItemDraftsFromHandle.value) {
                        val drafts = updatedItemDraftsFromHandle.value
                        if (drafts != null && drafts.isNotEmpty()) {
                            Log.d("ProjectDetail", "Received drafts: $drafts for project $projectId")
                            project?.id?.let { pId ->
                                projectViewModel.processProjectItemDraftsAndUpdateItems(
                                    projectId = pId,
                                    drafts = drafts,
                                    constructionItemViewModel = constructionItemViewModel
                                )
                            }
                            backStackEntry.savedStateHandle.remove<ArrayList<ProjectItemDraft>>("updatedProjectItemDrafts")
                            Log.d("ProjectDetail", "Cleared updatedProjectItemDrafts from SavedStateHandle")
                        }
                    }

                    if (project != null) {
                        ProjectDetailScreen(
                            project = project,
                            onConfirm = { updatedProjectData ->
                                projectViewModel.updateProject(updatedProjectData)
                                navController.navigate("projects") {
                                    popUpTo("projects") { inclusive = true }
                                }
                            },
                            navController = navController
                        )
                    }
                }
                composable(
                    "projectItems/{projectId}",
                    arguments = listOf(navArgument("projectId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val projectId = backStackEntry.arguments?.getInt("projectId")
                    val project = projectId?.let { projectViewModel.getProjectById(it) }
                    val projectItemsState = remember { mutableStateOf<List<ProjectItem>>(emptyList()) }
                    val constructionItemsState = remember { mutableStateOf<List<ConstructionItem>>(emptyList()) }

                    LaunchedEffect(projectId, projectsList, projectViewModel.projects.collectAsState().value) {
                        if (projectId != null) {
                            Log.d("ProjectItemsScreen", "Fetching items for projectId: $projectId")
                            val fetchedProjectItems = projectViewModel.getAllProjectItemsByProjectId(projectId)
                            projectItemsState.value = fetchedProjectItems
                            Log.d("ProjectItemsScreen", "Fetched project items: ${fetchedProjectItems.size}")

                            constructionItemsState.value = fetchedProjectItems.mapNotNull { projectItem ->
                                constructionItemViewModel.getItemById(projectItem.itemId)
                            }
                            Log.d("ProjectItemsScreen", "Fetched construction items: ${constructionItemsState.value.size}")
                        }
                    }


                    if (project != null) {
                        ProjectConstructionItemsScreen(
                            project = project,
                            navController = navController,
                            objectsList = projectItemsState.value,
                            constructionItems = constructionItemsState.value,
                        )
                    } else {
                        Log.w("ProjectItemsNav", "Project not found for ID: $projectId")
                    }
                }

                composable("constructionObjects") {
                    ConstructionItemList(
                        constructionObjectsList = constructionObjectsList,
                        navController = navController,
                        currentSearchQuery = currentSearchQuery,
                        onSearchQueryChanged = { query ->
                            constructionItemViewModel.setSearchQuery(query)
                        }
                    )
                }
                composable("addConstructionObject") {
                    AddConstructionItem(
                        onCancel = { navController.popBackStack() },
                        onConfirm = { newObject ->
                            constructionItemViewModel.insertItems(listOf(newObject))
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    "constructionObjectDetail/{constructionObjectId}",
                    arguments = listOf(navArgument("constructionObjectId") {
                        type = NavType.IntType
                    })
                ) { backStackEntry ->
                    val constructionObjectId =
                        backStackEntry.arguments?.getInt("constructionObjectId")
                    val constructionObject = remember { mutableStateOf<ConstructionItem?>(null) }


                    LaunchedEffect(constructionObjectId) {
                        constructionObject.value =
                            constructionObjectId?.let { constructionItemViewModel.getItemById(it) }
                    }

                    constructionObject.value?.let {
                        ConstructionItemDetailScreen(
                            constructionItem = it,
                            navController = navController,
                            onConfirm = { updatedObject ->
                                constructionItemViewModel.updateItem(updatedObject)
                                navController.popBackStack()
                            },
                            onDelete = { idToDelete ->
                                constructionItemViewModel.deleteItemById(idToDelete)
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}