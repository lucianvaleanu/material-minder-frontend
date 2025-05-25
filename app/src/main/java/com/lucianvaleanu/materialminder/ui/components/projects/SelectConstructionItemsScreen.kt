
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lucianvaleanu.materialminder.model.ConstructionItem
import com.lucianvaleanu.materialminder.model.ProjectItem
import com.lucianvaleanu.materialminder.ui.components.construction_item.ConstructionItemCardWithCount

@Composable
fun SelectConstructionItemsScreen(
    navController: NavController,
    constructionObjectsList: List<ConstructionItem>,
    projectItems: MutableList<ProjectItem>,
    onCancel: () -> Unit,
    onConfirm: (List<ProjectItem>) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var selectedItems by remember {
        mutableStateOf(
            projectItems.associate { it.itemId to it.quantity }.toMutableMap()
        )
    }

    val filteredList = constructionObjectsList.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search materials") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(filteredList.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { constructionObject ->
                        val quantity = selectedItems[constructionObject.id] ?: 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            ConstructionItemCardWithCount(
                                constructionItem = constructionObject,
                                navController = navController,
                                modifier = Modifier,
                                initialCount = quantity,
                                onCountChange = { newCount ->
                                    selectedItems = selectedItems.toMutableMap().apply {
                                        constructionObject.id?.let { put(it, newCount) }
                                    }
                                }
                            )
                        }
                    }
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = onCancel) {
                Text("Cancel")
            }
            Button(onClick = {
                val updatedItems = selectedItems.map { (itemId, quantity) ->
                    ProjectItem(
                        projectId = 0, // Temporary ID, will be updated later
                        itemId = itemId,
                        quantity = quantity
                    )
                }
                navController.previousBackStackEntry?.savedStateHandle?.set("selectedMaterials", updatedItems)
                navController.popBackStack()
            }) {
                Text("Confirm")
            }
        }
    }
}
