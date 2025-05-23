package com.lucianvaleanu.materialminder.ui.components.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lucianvaleanu.materialminder.R
import com.lucianvaleanu.materialminder.model.Project


@Composable
fun ProjectsList(
    projectsList: List<Project>,
    navController: NavController,
    modifier: Modifier = Modifier,
    onDelete: (Project) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Projects",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                IconButton(onClick = { navController.navigate("constructionObjects") }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.wheelbarrow),
                        contentDescription = "Icon",
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(24.dp)
                    )
                }
            }
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                items(projectsList, key = { it.id }) { project ->
                    ProjectListItem(
                        project = project,
                        onDelete = onDelete,
                        navController = navController
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { navController.navigate("addProject") },
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

