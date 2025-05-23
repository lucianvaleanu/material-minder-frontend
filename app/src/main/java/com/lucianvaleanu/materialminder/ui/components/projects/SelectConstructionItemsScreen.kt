package com.lucianvaleanu.materialminder.ui.components.projects

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.lucianvaleanu.materialminder.R
import com.lucianvaleanu.materialminder.model.ConstructionItem
import com.lucianvaleanu.materialminder.model.ProjectItem

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

        val filteredList = constructionObjectsList.filter {
            it.name.contains(searchText, ignoreCase = true)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredList) { constructionObject ->
                val quantity = selectedItems[constructionObject.id] ?: 0

                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(constructionObject.image),
                            contentDescription = constructionObject.name,
                            modifier = Modifier
                                .size(80.dp)
                                .padding(bottom = 8.dp)
                        )
                        Text(
                            text = constructionObject.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = {
                                if (quantity > 0) {
                                    selectedItems = selectedItems.toMutableMap().apply {
                                        put(constructionObject.id, quantity - 1)
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_remove_24),
                                    contentDescription = "Remove"
                                )
                            }
                            Text(
                                text = quantity.toString(),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = {
                                selectedItems = selectedItems.toMutableMap().apply {
                                    put(constructionObject.id, quantity + 1)
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }
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
                onConfirm(updatedItems)
                navController.popBackStack()
            }) {
                Text("Confirm")
            }
        }
    }
}