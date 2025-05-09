package com.valeanulucian.materialminder.ui.components.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.valeanulucian.materialminder.R
import com.valeanulucian.materialminder.model.ConstructionObjectWithCount
import com.valeanulucian.materialminder.model.Project
import java.time.LocalDate

@Composable
fun AddProjectScreen(
    navController: NavController,
    onAddProject: (Project) -> Unit,
    onCancel: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }  // Use rememberSaveable to persist across recompositions
    var date by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var objectsList by rememberSaveable {
        mutableStateOf<List<ConstructionObjectWithCount>>(
            emptyList()
        )
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    // Observe selected materials from savedStateHandle
    val selectedMaterials = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<List<ConstructionObjectWithCount>>("selectedMaterials")
        ?.observeAsState()

    if (selectedMaterials != null) {
        LaunchedEffect(selectedMaterials.value) {
            selectedMaterials.value?.let {
                objectsList = it
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Create new project",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.Start)
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Project Title") },
            trailingIcon = {
                IconButton(onClick = { title = "" }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear title")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = date.toString(),
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
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    date = LocalDate.of(year, month + 1, dayOfMonth)
                    showDatePicker = false
                },
                date.year,
                date.monthValue - 1,
                date.dayOfMonth
            ).show()
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { navController.navigate("selectMaterials") },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.wheelbarrow),
                contentDescription = "Add materials",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add materials")
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            FloatingActionButton(
                onClick = {
                    val newProject = Project(
                        id = 0,
                        title = title,
                        date = date,
                        objectsList = objectsList
                    )
                    onAddProject(newProject)
                    navController.popBackStack()
                },
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "OK",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

    }
}
