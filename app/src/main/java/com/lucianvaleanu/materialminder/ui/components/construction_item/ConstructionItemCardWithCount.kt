package com.lucianvaleanu.materialminder.ui.components.construction_item

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.lucianvaleanu.materialminder.R
import com.lucianvaleanu.materialminder.model.ConstructionItem
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController

@Composable
fun ConstructionItemCardWithCount(
    constructionItem: ConstructionItem,
    modifier: Modifier = Modifier,
    navController: NavController,
    initialCount: Int = 0,
    onCountChange: (Int) -> Unit = {}
) {
    var count by remember { mutableIntStateOf(initialCount) }
    var showDialog by remember { mutableStateOf(false) }
    var editingText by remember { mutableStateOf(count.toString()) }

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("constructionObjectDetail/${constructionItem.id}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val painter = if (constructionItem.image.isNotEmpty()) {
                rememberAsyncImagePainter(Uri.parse(constructionItem.image))
            } else {
                painterResource(id = R.drawable.image_placeholder)
            }

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = constructionItem.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { if (count > 0) { count--; onCountChange(count) } }) {
                    Icon(imageVector = ImageVector.vectorResource(R.drawable.baseline_remove_24), contentDescription = "Decrease")
                }
                if (count > 0) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable {
                                editingText = count.toString()
                                showDialog = true
                            }) {
                        Text(
                            text = count.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(32.dp))
                }
                IconButton(
                    onClick = { count++; onCountChange(count) }) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Set Quantity") },
            text = {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) editingText = newValue
                    },
                    label = { Text("Quantity") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    count = editingText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    onCountChange(count)
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            })
    }

}


@Preview(showBackground = true)
@Composable
fun PreviewConstructionItemCardWithCount() {
    val mockItem = ConstructionItem(
        id = 1,
        name = "Concrete Block",
        image = "",
        price = TODO(),
        // Add other required fields if any
    )
    ConstructionItemCardWithCount(
        constructionItem = mockItem,
        navController = rememberNavController(),
        initialCount = 2
    )
}