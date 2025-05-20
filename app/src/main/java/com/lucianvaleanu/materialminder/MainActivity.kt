package com.lucianvaleanu.materialminder

import AddConstructionItem
import android.content.Context
import android.os.Bundle
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lucianvaleanu.materialminder.model.ConstructionItem
import com.lucianvaleanu.materialminder.ui.theme.MaterialMinderTheme
import com.lucianvaleanu.materialminder.ui.components.*
import com.lucianvaleanu.materialminder.ui.components.construction_item.ConstructionItemList
import com.lucianvaleanu.materialminder.ui.components.construction_item.ConstructionItemDetailScreen
import com.lucianvaleanu.materialminder.viewmodel.ConstructionItemViewModel
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val constructionItemViewModel: ConstructionItemViewModel by viewModels<ConstructionItemViewModel>()
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

                    )
            }
        }
    }
}

@Composable
fun MaterialMinder(
    modifier: Modifier = Modifier,
    constructionItemViewModel: ConstructionItemViewModel,
    ) {
    val navController = rememberNavController()

    val constructionObjectsList by constructionItemViewModel.constructionItems.collectAsState()

    var shouldShowOnboarding by remember { mutableStateOf(true) }

    Surface(modifier = modifier) {
        if (shouldShowOnboarding) {
            OnboardingScreen(onTimeout = { shouldShowOnboarding = false })
        } else {
            NavHost(navController = navController, startDestination = "constructionObjects") {
//                composable("projects") {
//                    ProjectsList(
//                        projectsList = projectsList,
//                        navController = navController,
//                        onDelete = { project -> projectViewModel.deleteProject(project.id) }
//                    )
//                }
//                composable("addProject") {
//                    AddProjectScreen(
//                        navController,
//                        onAddProject = { project -> projectViewModel.insertProjects(listOf(project)) },
//                        onCancel = { navController.popBackStack() }
//                    )
//                }
//                composable("selectMaterials") {
//                    SelectConstructionItemsScreen(
//                        navController = navController,
//                        constructionObjectsList = constructionObjectsList,
//                        onConfirm = { selectedObjects ->
//                            navController.previousBackStackEntry?.savedStateHandle?.set("selectedMaterials", selectedObjects)
//                            navController.popBackStack()
//                        },
//                        onCancel = { navController.popBackStack() }
//                    )
//                }
//                composable(
//                    "projectDetail/{projectId}",
//                    arguments = listOf(navArgument("projectId") {
//                        type = NavType.IntType
//                    })
//                ) { backStackEntry ->
//                    val projectId = backStackEntry.arguments?.getInt("projectId")
//                    val project = projectId?.let { projectViewModel.getProjectById(it) }
//
//                    if (project != null) {
//                        ProjectDetailScreen(
//                            project = project,
//                            onConfirm = { updatedProject -> projectViewModel.updateProject(updatedProject) },
//                            navController = navController
//                        )
//                    }
//                }
//
//                composable(
//                    "projectItems/{projectId}",
//                    arguments = listOf(navArgument("projectId") { type = NavType.IntType })
//                ) { backStackEntry ->
//                    val projectId = backStackEntry.arguments?.getInt("projectId")
//                    val project = projectId?.let { projectViewModel.getProjectById(it) }
//
//                    if (project != null) {
//                        ProjectConstructionItemsScreen(
//                            project = project,
//                            navController = navController
//                        )
//                    }
//                }
//
                composable("constructionObjects") {
                    ConstructionItemList(
                        constructionObjectsList = constructionObjectsList,
                        navController = navController
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
                    val constructionObjectId = backStackEntry.arguments?.getInt("constructionObjectId")
                    val constructionObject = remember(constructionObjectId) {
                        mutableStateOf<ConstructionItem?>(null)
                    }

                    LaunchedEffect(constructionObjectId) {
                        constructionObject.value = constructionObjectId?.let { constructionItemViewModel.getItemById(it) }
                    }

                    if(constructionObject.value != null) {
                        ConstructionItemDetailScreen(
                            constructionItem = constructionObject.value!!,
                            navController = navController,
                            onConfirm = { updatedObject ->
                                constructionItemViewModel.updateItem(updatedObject)
                                navController.popBackStack()
                            },
                            onDelete = { idToDelete ->
                                constructionItemViewModel.deleteItemById(idToDelete)
                                navController.popBackStack()
                            }
                        )                    }

                }
            }
        }
    }
}