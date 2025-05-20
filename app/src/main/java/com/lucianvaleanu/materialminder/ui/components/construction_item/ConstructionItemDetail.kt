package com.lucianvaleanu.materialminder.ui.components.construction_item

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.lucianvaleanu.materialminder.R
import com.lucianvaleanu.materialminder.model.ConstructionItem
import java.math.BigDecimal

@Composable
fun ConstructionItemDetailScreen(
    constructionItem: ConstructionItem,
    navController: NavController,
    onConfirm: (ConstructionItem) -> Unit,
    onDelete: (Int) -> Unit
) {
    var updatedName by remember { mutableStateOf(constructionItem.name) }
    var updatedPrice by remember { mutableStateOf(constructionItem.price.toString()) }
    var updatedImageUri by remember { mutableStateOf(constructionItem.image) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            updatedImageUri = uri?.toString() ?: constructionItem.image
        })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        val painter = if (updatedImageUri.isNotEmpty()) {
            rememberAsyncImagePainter(Uri.parse(updatedImageUri))
        } else {
            painterResource(id = R.drawable.image_placeholder)
        }

        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = updatedName,
            onValueChange = { updatedName = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = updatedPrice,
            onValueChange = { updatedPrice = it },
            label = { Text("Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_file_upload_24),
                    contentDescription = "Change Image",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Change Image")
            }

            Button(
                onClick = {
                    showDeleteDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete object",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
            FloatingActionButton(
                onClick = {
                    onConfirm(
                        ConstructionItem(
                            id = constructionItem.id,
                            name = updatedName,
                            price = updatedPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                            image = updatedImageUri
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Confirm Deletion") },
            text = { Text("Are you sure you want to delete this object?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(constructionItem.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
