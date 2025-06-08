package com.lucianvaleanu.materialminder.ui.components.construction_item

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.lucianvaleanu.materialminder.R
import com.lucianvaleanu.materialminder.model.ConstructionItem
import androidx.core.net.toUri

@Composable
fun ConstructionItemCardWithCount(
    constructionItem: ConstructionItem,
    modifier: Modifier = Modifier,
    navController: NavController,
    initialCount: Int,
    onCountChange: (Int) -> Unit = {}
) {
    var count by remember { mutableIntStateOf(initialCount) }
    var showDialog by remember { mutableStateOf(false) }
    var editingText by remember { mutableStateOf(initialCount.toString()) }

    LaunchedEffect(initialCount) {
        if (count != initialCount) {
            count = initialCount
            if (!showDialog) {
                editingText = initialCount.toString()
            }
        }
    }

    LaunchedEffect(count) {
        if (!showDialog && editingText != count.toString()) {
            editingText = count.toString()
        }
    }

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                constructionItem.id?.let {
                    navController.navigate("constructionObjectDetail/$it")
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val imageUri = try {
                constructionItem.image.takeIf { it.isNotBlank() }?.toUri()
            } catch (_: Exception) {
                null
            }

            Image(
                painter = if (imageUri != null) {
                    rememberAsyncImagePainter(
                        model = imageUri,
                        error = painterResource(id = R.drawable.image_placeholder)
                    )
                } else {
                    painterResource(id = R.drawable.image_placeholder)
                },
                contentDescription = constructionItem.name,
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = constructionItem.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Price: ${constructionItem.price} RON",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    if (count > 0) {
                        val newCount = count - 1
                        count = newCount
                        onCountChange(newCount)
                    }
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_remove_24),
                        contentDescription = "Decrease count"
                    )
                }

                Text(
                    text = "$count",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        editingText = count.toString()
                        showDialog = true
                    }
                )

                IconButton(onClick = {
                    val newCount = count + 1
                    count = newCount
                    onCountChange(newCount)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase count")
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Set Quantity for ${constructionItem.name}") },
            text = {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = { editingText = it.filter { char -> char.isDigit() } },
                    label = { Text("Quantity") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val newCount = editingText.toIntOrNull() ?: count
                    count = newCount
                    onCountChange(newCount)
                    showDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}