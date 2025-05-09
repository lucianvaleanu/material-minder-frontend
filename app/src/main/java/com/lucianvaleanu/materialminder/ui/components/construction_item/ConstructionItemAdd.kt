import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import com.valeanulucian.materialminder.R
import com.valeanulucian.materialminder.model.ConstructionItem

@Composable
fun AddConstructionItem(onCancel: () -> Unit, onConfirm: (ConstructionItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Add a new construction object",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start),
            fontSize = 24.sp
        )

        OutlinedTextField(
            label = { Text("Name") },
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            label = { Text("Description") },
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            label = { Text("Price") },
            value = price,
            onValueChange = { price = it },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier
                .padding(vertical = 16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_file_upload_24),
                contentDescription = "Add Image",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Image")
        }

//        // TODO Display selected image if available
//        imageUri?.let { uri ->
//            Image(
//                painter = rememberAsyncImagePainter(uri),
//                contentDescription = "Selected Image",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .aspectRatio(1f)
//                    .clip(MaterialTheme.shapes.medium)
//                    .border(1.dp, Color.Gray, MaterialTheme.shapes.medium)
//            )
//        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onCancel, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_close_24),
                    contentDescription = "Cancel"
                )
            }
            IconButton(
                onClick = {
                    onConfirm(
                        ConstructionItem(
                            id = 0,
                            name = name,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            image = imageUri?.toString() ?: ""
                        )
                    )
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_check_24),
                    contentDescription = "Confirm"
                )
            }
        }
    }
}


