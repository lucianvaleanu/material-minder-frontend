package com.lucianvaleanu.materialminder.ui.components.projects

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lucianvaleanu.materialminder.R
import com.lucianvaleanu.materialminder.model.Project
import java.time.LocalDate

@Composable
fun ProjectDetailScreen(
    project: Project,
    navController: NavController,
    onConfirm: (Project) -> Unit
) {
    var updatedTitle by remember { mutableStateOf(project.title) }
    var updatedDate by remember { mutableStateOf(project.projectDate) }

    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(text = project.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = updatedTitle,
            onValueChange = { updatedTitle = it },
            label = { Text("Project Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = updatedDate.toString(),
            onValueChange = {},
            label = { Text("Date") },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_calendar_today_24),
                        contentDescription = "Select date"
                    )
                }
            },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (showDatePicker) {
            val context = LocalContext.current
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    updatedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    showDatePicker = false
                },
                updatedDate.year,
                updatedDate.monthValue - 1,
                updatedDate.dayOfMonth
            ).show()
        }

        Column(modifier = Modifier.fillMaxWidth()){
            Button(
                onClick = { navController.navigate("projectItems/${project.id}") },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(12.dp))
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = "View Items")
            }

            Button(
                onClick = {
                    navController.navigate("selectMaterials") {
                        //TODO : Pass the project ID to the SelectConstructionItemsScreen
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(12.dp))
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = "Change Items")
            }
        }


        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black
                )
            }

            FloatingActionButton(
                onClick = {
                    //TODO: implement the conjoined table for project and construction items
                    val updatedProject = project.copy(
                        title = updatedTitle,
                        projectDate = updatedDate
                    )
                    onConfirm(updatedProject)
                    navController.popBackStack()
                },
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint = Color.Black
                )
            }
        }
    }
}
