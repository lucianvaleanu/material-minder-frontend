package com.valeanulucian.materialminder.ui.components.projects

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.valeanulucian.materialminder.R
import com.valeanulucian.materialminder.model.ConstructionItem
import com.valeanulucian.materialminder.model.ConstructionObjectWithCount

@Composable
fun SelectConstructionItemsScreen(
    navController: NavController,
    constructionObjectsList: List<ConstructionItem>,
    onConfirm: (List<ConstructionObjectWithCount>) -> Unit,
    onCancel: () -> Unit
) {
    val existingObjectsList = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<List<ConstructionObjectWithCount>>("currentObjectsList")

    var searchText by remember { mutableStateOf("") }
    var selectedItems by remember {
        mutableStateOf(existingObjectsList?.associate { it.constructionItem.id to it.count }?.toMutableMap() ?: mutableMapOf())
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

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
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
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val painter = if (constructionObject.image.isNotEmpty()) {
                            rememberAsyncImagePainter(Uri.parse(constructionObject.image))
                        } else {
                            painterResource(id = R.drawable.image_placeholder)
                        }
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(MaterialTheme.shapes.small)
                                .padding(end = 8.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
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
                                    Icon(imageVector = ImageVector.vectorResource(id = R.drawable.baseline_remove_24), contentDescription = "Remove")
                                }
                                Text(text = quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp))
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
                val selectedConstructionObjectsWithCount = constructionObjectsList
                    .filter { (selectedItems[it.id] ?: 0) > 0 }
                    .map { constructionObject ->
                        ConstructionObjectWithCount(
                            constructionItem = constructionObject,
                            count = selectedItems[constructionObject.id] ?: 0
                        )
                    }

                // Save to savedStateHandle to persist counts
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "currentObjectsList",
                    selectedConstructionObjectsWithCount
                )

                // Call onConfirm with updated list
                onConfirm(selectedConstructionObjectsWithCount)
            }) {
                Text("Confirm")
            }

        }
    }
}
