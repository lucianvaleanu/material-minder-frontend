package com.lucianvaleanu.materialminder.ui.components.construction_item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lucianvaleanu.materialminder.R
import com.lucianvaleanu.materialminder.model.ConstructionItem
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ConstructionItemList(
    constructionObjectsList: List<ConstructionItem>,
    modifier: Modifier = Modifier,
    navController: NavController,
    currentSearchQuery: String,
    onSearchQueryChanged: (String) -> Unit
) {
    var searchText by remember { mutableStateOf(currentSearchQuery) }

    LaunchedEffect(currentSearchQuery) {
        if (searchText != currentSearchQuery) {
            searchText = currentSearchQuery
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(24.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            item {
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "All construction objects",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        onSearchQueryChanged(it)
                    },
                    label = { Text("Search materials") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            items(constructionObjectsList.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { constructionObject ->
                        ConstructionItemCard(
                            constructionItem = constructionObject,
                            modifier = Modifier.weight(1f),
                            navController = navController
                        )
                    }
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate("addConstructionObject") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .size(48.dp),
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_add_24),
                contentDescription = null
            )
        }
    }
}