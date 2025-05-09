package com.valeanulucian.materialminder

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
import com.valeanulucian.materialminder.ui.theme.MaterialMinderTheme
import com.valeanulucian.materialminder.ui.components.*
import com.valeanulucian.materialminder.ui.components.construction_item.ConstructionItemList
import com.valeanulucian.materialminder.ui.components.construction_item.ConstructionItemDetailScreen
import com.valeanulucian.materialminder.ui.components.projects.AddProjectScreen
import com.valeanulucian.materialminder.ui.components.projects.ProjectDetailScreen
import com.valeanulucian.materialminder.ui.components.projects.ProjectConstructionItemsScreen
import com.valeanulucian.materialminder.ui.components.projects.ProjectsList
import com.valeanulucian.materialminder.ui.components.projects.SelectConstructionItemsScreen
import com.valeanulucian.materialminder.viewmodel.ConstructionItemViewModel
import com.valeanulucian.materialminder.viewmodel.ProjectViewModel
class MainActivity : ComponentActivity() {
    private val constructionItemViewModel: ConstructionItemViewModel by viewModels()
    private val projectViewModel: ProjectViewModel by viewModels()

    companion object {
        private var appContext: Context? = null

        fun getAppContext(): Context? {
            return appContext
        }
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
    modifier: Modifier = Modifier, constructionItemViewModel: ConstructionItemViewModel,
    projectViewModel: ProjectViewModel
) {
    val navController = rememberNavController()
    val projectsList by projectViewModel.projects.collectAsState()
    val constructionObjectsList by constructionItemViewModel.constructionObjects.collectAsState()
    var shouldShowOnboarding by remember { mutableStateOf(true) }
    Surface(modifier = modifier) {
        if (shouldShowOnboarding) {
            OnboardingScreen(onTimeout = { shouldShowOnboarding = false })
        } else {
            NavHost(navController = navController, startDestination = "projects") {
                composable("projects") {
                    ProjectsList(
                        projectsList = projectsList,
                        navController = navController,
                        onDelete = { project -> projectViewModel.deleteProject(project.id) }
                    )
                }
                composable("addProject") {
                    AddProjectScreen(
                        navController,
                        onAddProject = { project -> projectViewModel.addProject(project) }) {
                    }
                }
                composable("selectMaterials") {
                    SelectConstructionItemsScreen(
                        navController = navController,
                        constructionObjectsList = constructionObjectsList,
                        onConfirm = { selectedObjects ->
                            navController.previousBackStackEntry?.savedStateHandle?.set("selectedMaterials", selectedObjects)
                            navController.popBackStack()
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }
                composable(
                    "projectDetail/{projectId}",
                    arguments = listOf(navArgument("projectId") {
                        type = NavType.IntType
                    })
                ) { backStackEntry ->
                    val projectId = backStackEntry.arguments?.getInt("projectId")
                    val project = projectId?.let {
                        projectViewModel.getProject(projectId)
                    }

                    if (project != null) {
                        ProjectDetailScreen(
                            project = project,
                            onConfirm = { project -> projectViewModel.updateProject(project) },
                            navController = navController)
                    }
                }

                composable(
                    "projectItems/{projectId}",
                    arguments = listOf(navArgument("projectId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val projectId = backStackEntry.arguments?.getInt("projectId")
                    val project = projectId?.let { projectViewModel.getProject(it) }

                    if (project != null) {
                        ProjectConstructionItemsScreen(
                            project = project,
                            navController = navController
                        )
                    }
                }

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
                            constructionItemViewModel.addConstructionObject(newObject)
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
                    val constructionObject =
                        constructionObjectId?.let {
                            constructionItemViewModel.getConstructionObjectById(
                                it
                            )
                        }

                    if (constructionObject != null) {
                        ConstructionItemDetailScreen(
                            constructionItem = constructionObject,
                            navController = navController,
                            onConfirm = { updatedObject ->
                                constructionItemViewModel.updateConstructionObject(updatedObject)
                                navController.popBackStack()

                            },
                            onDelete = { idToDelete ->
                                constructionItemViewModel.deleteConstructionObjectById(idToDelete)
                                navController.popBackStack()
                            })

                    }
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MyAppPreview() {
//    MaterialMinderTheme {
//        MaterialMinder()
//    }
//}